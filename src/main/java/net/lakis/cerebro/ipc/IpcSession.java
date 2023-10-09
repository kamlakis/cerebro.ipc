package net.lakis.cerebro.ipc;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.config.IpcClientConfig;
import net.lakis.cerebro.ipc.config.IpcConfig;
import net.lakis.cerebro.ipc.ipm.Ipm;
import net.lakis.cerebro.ipc.ipm.IpmBindRequest;
import net.lakis.cerebro.ipc.ipm.IpmBindResponse;
import net.lakis.cerebro.ipc.ipm.IpmData;
import net.lakis.cerebro.ipc.ipm.IpmEnquireLinkResponse;
import net.lakis.cerebro.ipc.workers.EnquireLinkTimedWorker;
import net.lakis.cerebro.ipc.workers.IpmReceiverWorker;
import net.lakis.cerebro.ipc.workers.IpmSenderConsumer;
import net.lakis.cerebro.jobs.prosumer.Prosumer;
import net.lakis.cerebro.jobs.prosumer.ProsumerFactory;
import net.lakis.cerebro.socket.SocketFactory;
import net.lakis.cerebro.socket.client.ISocket;
import net.lakis.cerebro.socket.exceptions.SocketClientCreateException;

@Log4j2
@Accessors(fluent = true, chain = true)
public class IpcSession {
	public static boolean StdoutDebug = false;
//	public @Getter long id = System.currentTimeMillis();
	protected @Getter IpcConfig config;
	private @Getter SocketFactory socketFactory;

	private @Getter List<IpcSessionStateListener> sessionStateListeners;

	private @Getter ISocket socket;

	protected @Getter volatile IpcSessionState state = IpcSessionState.CLOSED;
	private volatile int sequence;
	private @Getter long lastActivity;

	private Prosumer<Ipm> senderProsumer;
	private IpmReceiverWorker receiverWorker;
	private EnquireLinkTimedWorker enquireLinkTimedWorker;
	private boolean isClient;
//	private @Getter @Setter String localAppId;
	private @Getter @Setter String remoteAppId;
	private Object userData;
	private Object configData;
	private IpcSessionsPool pool;

	/**
	 * Constructor for Session.
	 */
	public IpcSession(IpcSessionsPool pool, IpcConfig config, List<IpcSessionStateListener> sessionStateListeners,
			SocketFactory socketFactory) {
		this.pool = pool;

		this.config = config;
		this.socketFactory = socketFactory;
//		this.localAppId = config.appId();
		this.isClient = (config instanceof IpcClientConfig);

		this.sessionStateListeners = sessionStateListeners;

		this.senderProsumer = ProsumerFactory.createBlockingProsumer(getName() + "_IpmSenderProsumer",
				new IpmSenderConsumer(this));
		this.receiverWorker = new IpmReceiverWorker(this);
		if (config.enquireLinkTimer() > 0)
			this.enquireLinkTimedWorker = new EnquireLinkTimedWorker(this, config.enquireLinkTimer());

		this.touch();
	}

	private void touch() {
		this.lastActivity = System.currentTimeMillis();
	}

	public boolean isClosed() {
		return state == IpcSessionState.CLOSED;
	}

	public void setState(IpcSessionState state) {
		try {
			if (this.state == state)
				return;
			IpcSessionState oldState = this.state;
			this.state = state;

			for (IpcSessionStateListener listener : sessionStateListeners) {
				listener.stateChanged(this, oldState, state);

			}

			if (this.state != IpcSessionState.BOUND)
				this.remoteAppId = null;
		} catch (Exception e) {
			log.error("Exception", e);
			this.state = IpcSessionState.UNBOUND;
		}
		if (config.closeWhenUnbound() && state == IpcSessionState.UNBOUND) {
			close();
		}
	}

	/**
	 * Processes exception occurred on link. Closes session.
	 *
	 * @param ioe exception
	 */
	public synchronized void onIOException(IOException ioe) {
		if (this.state == IpcSessionState.CLOSED) {
			log.debug("Link is closing on {}", getName());
		} else if (ioe instanceof EOFException) {
			log.debug("EOF on link on " + getName(), ioe);
			close();
		} else {
			log.error("Link error on " + getName(), ioe);
			close();
		}
	}

	public synchronized void send(Ipm ipm) {
		this.senderProsumer.handleIfRunning(ipm);
	}

	public int getTimeout() {
		return this.config.timeout();
	}

	public synchronized void close() {
		if (this.state == IpcSessionState.CLOSED) {
			log.debug("Session already closed");
			return;
		}
		setState(IpcSessionState.CLOSED);

		// Stop auto enquire link
		if (this.enquireLinkTimedWorker != null) {
			enquireLinkTimedWorker.stop();
		}
		this.receiverWorker.stop();

		this.senderProsumer.stopWorkers();
		this.senderProsumer.clear();

		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				log.error("Closing socket failed", e);
			} finally {
				socket = null;
			}
		}
		this.pool.removeBoundSession(this);

		log.debug("socket closed");
	}

	public synchronized void open()
			throws SocketClientCreateException, InterruptedException, ExecutionException, TimeoutException {
		if (this.state != IpcSessionState.CLOSED) {
			log.debug("Session already open");
			return;
		}
		this.sequence = 0;
		this.socket = socketFactory.createSocket();

		this.senderProsumer.startWorkers();
		this.receiverWorker.start();

		if (this.enquireLinkTimedWorker != null) {
			enquireLinkTimedWorker.start();
		}

		setState(IpcSessionState.OPEN);
	}

	/**
	 * binds from remote entity.
	 *
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void bind() throws InterruptedException, ExecutionException, TimeoutException {
		bind(config.timeout());
	}

	/**
	 * binds from remote entity.
	 * 
	 * @param timeout maximum time in ms to wait for response. If set to 0 wait
	 *                forever.
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void bind(long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		if (!isClient)
			throw new ExecutionException("only client session can bind to server peer", null);
		try {
			this.state = IpcSessionState.UNBOUND;
			this.send(new IpmBindRequest().appId(config.appId()));
			synchronized (this) {
				this.wait();
			}
			if (this.state != IpcSessionState.BOUND)
				throw new ExecutionException("bind failed", null);

		} catch (Exception e) {
			setState(IpcSessionState.UNBOUND);
			throw new ExecutionException("bind failed.", e);
		}
	}

//	/**
//	 * Unbinds from remote entity.
//	 *
//	 * @throws TimeoutException
//	 * @throws ExecutionException
//	 * @throws InterruptedException
//	 */
//	public void unbind() throws InterruptedException, ExecutionException, TimeoutException {
//		unbind(config.timeout());
//	}

	/**
	 * Unbinds from remote entity.
	 *
	 * @param timeout maximum time in ms to wait for response. If set to 0 wait
	 *                forever.
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
//	public void unbind(long timeout) throws InterruptedException, ExecutionException, TimeoutException {
//		this.send(new UnbindIpm());
////		Future<IpmResponse> future = this.send(new UnbindIpm());
//
////		IpmResponse response = future.get(timeout, TimeUnit.MILLISECONDS);
////		
////		if (response == null)
////			throw new ExecutionException("Unbind failed. repsonse null", null);
////
////		if (!response.resultCode().isSuccess())
////			throw new ExecutionException("Unbind failed. repsonse " + response.resultCode(), null);
//
//		setState(IpcSessionState.UNBOUND);
//	}

//	public void unbindAndClose() throws InterruptedException, ExecutionException, TimeoutException {
//		try {
//			if (state == IpcSessionState.BOUND) {
//				unbind();
//			}
//		} finally {
//			close();
//		}
//	}

	@Override
	public String toString() {
		try {
			if (socket != null)
				return socket.toString();
		} catch (Exception e) {
		}
		return socketFactory.toString();
	}

	@SuppressWarnings("unchecked")
	public <T> T getUserData(Class<T> type) {
		if (userData != null && type.isAssignableFrom(userData.getClass()))
			return (T) userData;
		return null;
	}

	public Object getUserData() {
		return userData;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public Object getConfigData() {
		return configData;
	}

	public void setConfigData(Object configData) {
		this.configData = configData;
	}

	public String getName() {
		return config().name() + "_" + config().appId() + "_" + socketFactory.toString();
	}

	public void handleBindRequest(IpmBindRequest request) {
		this.remoteAppId = request.appId();
		this.setState(IpcSessionState.BOUND);
		this.send(new IpmBindResponse().appId(this.config.appId()));
	}

	public void handleBindResponse(IpmBindResponse response) {
		this.remoteAppId = response.appId();
		this.setState(IpcSessionState.BOUND);
		synchronized (this) {
			this.notifyAll();
		}
	}

	public void handleEnquireLinkRequest() {
		this.send(new IpmEnquireLinkResponse());
	}

	public void handleEnquireLinkResponse() {
		this.enquireLinkTimedWorker.responded();
	}

	public void handleData(IpmData data) {
		Consumer<IpmData> handler = this.pool.ipmDataHandler();
		if (handler != null)
			handler.accept(data);
	}

}