package net.lakis.cerebro.ipc;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.config.IpcClientConfig;
import net.lakis.cerebro.ipc.config.IpcConfig;
import net.lakis.cerebro.ipc.event.SessionStateChangeEvent;
import net.lakis.cerebro.ipc.exceptions.SessionException;
import net.lakis.cerebro.ipc.ipm.Ipm;
import net.lakis.cerebro.ipc.processors.BindProcessor;
import net.lakis.cerebro.ipc.processors.IpmProcessor;
import net.lakis.cerebro.ipc.processors.PingProcessor;
import net.lakis.cerebro.ipc.socket.SocketFactory;
import net.lakis.cerebro.ipc.socket.TcpSocket;
import net.lakis.cerebro.ipc.socket.exceptions.SocketClientCreateException;
import net.lakis.cerebro.ipc.workers.IpmReceiverWorker;
import net.lakis.cerebro.ipc.workers.IpmSenderConsumer;
import net.lakis.cerebro.ipc.workers.PingTimedWorker;
import net.lakis.cerebro.jobs.async.AsyncExecutor;
import net.lakis.cerebro.jobs.async.AsyncResponseHandler;
import net.lakis.cerebro.jobs.prosumer.Prosumer;
import net.lakis.cerebro.jobs.prosumer.ProsumerFactory;

@Log4j2
@Accessors(fluent = true, chain = true)
public class IpcSession {
	public static boolean StdoutDebug = false;
//	public @Getter long id = System.currentTimeMillis();
	protected @Getter IpcConfig config;
	private @Getter SocketFactory socketFactory;

	private @Getter List<IpcSessionStateListener> sessionStateListeners;
	private @Getter Set<IpmProcessor> ipmProcessors;

	private @Getter TcpSocket socket;

	protected @Getter volatile IpcSessionState state = IpcSessionState.CLOSED;
	private volatile int sequence;
	private @Getter long lastActivity;

	private Prosumer<Ipm> senderProsumer;
	private IpmReceiverWorker receiverWorker;
	private AsyncExecutor<Ipm> asyncExecutor;
	private PingTimedWorker pingTimedWorker;
	private boolean isClient;
	private @Getter @Setter String localAppId;
	private @Getter @Setter String remoteAppId;
	private Object userData;
	private Object configData;
	private IpcSessionsPool pool;

	/**
	 * Constructor for Session.
	 */
	public IpcSession(IpcSessionsPool pool, IpcConfig config, Set<IpmProcessor> ipmProcessors,
			List<IpcSessionStateListener> sessionStateListeners, SocketFactory socketFactory) {
		this.pool = pool;

		this.config = config;
		this.socketFactory = socketFactory;
		this.localAppId = config.appId();
		this.isClient = (config instanceof IpcClientConfig);

		this.ipmProcessors = ipmProcessors;
		this.sessionStateListeners = sessionStateListeners;

		this.senderProsumer = ProsumerFactory.createBlockingProsumer(getName() + "_IpmSenderProsumer",
				new IpmSenderConsumer(this));
		this.receiverWorker = new IpmReceiverWorker(this);
		this.asyncExecutor = new AsyncExecutor<Ipm>(getName() + "_AsyncExecutor", config.incomingThreads(),
				config.timeout());
		if (config.pingTimer() > 0)
			this.pingTimedWorker = new PingTimedWorker(this, config.pingTimer());

		this.touch();
	}

	private void touch() {
		this.lastActivity = System.currentTimeMillis();
	}

	public boolean isClosed() {
		return state == IpcSessionState.CLOSED;
	}

	public void setState(IpcSessionState state) {
		if (this.state == state)
			return;

		this.asyncExecutor.execute(new SessionStateChangeEvent(this, this.state, state)

				, 1000);
//		try {
//			for (IpcSessionStateListener listener : sessionStateListeners) {
//				listener.stateChanged(this, this.state, state);
//			}
//		} catch (Exception e) {
//			log.error("Exception: ", e);
//			if(state == IpcSessionState.BOUND)
//				this.setState(IpcSessionState.UNBOUND);
//			return;
//		}

		log.trace("set State {} appId {}", state, remoteAppId);
		this.state = state;
		if (this.state != IpcSessionState.BOUND)
			this.remoteAppId = null;

		log.trace("set State {} appId {} done", state, remoteAppId);

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

	public synchronized void send(Ipm ipm, IpmResponseHandler responseHandler) {
		ipm.sequence(++sequence);
		this.asyncExecutor.schedule(ipm.sequence(), new AsyncResponseHandler<Ipm>() {

			@Override
			public void onTimeout() {
				responseHandler.onResponse(null, new TimeoutException());
			}

			@Override
			public void onResponse(Ipm response) {
				responseHandler.onResponse(response, null);				
			}
		});
		if (this.senderProsumer.handleIfRunning(ipm) == false)
			this.asyncExecutor.responded(ipm.sequence(), null);
	}

	public synchronized Future<Ipm> sendRequest(Ipm ipm) {
		Future<Ipm> future = null;

		ipm.sequence(++sequence);
		future = this.asyncExecutor.schedule(ipm.sequence());

		if (this.senderProsumer.handleIfRunning(ipm) == false)
			this.asyncExecutor.responded(ipm.sequence(), null);
		return future;
	}

	public void handle(Ipm ipm) {
		if (ipm.tag() == Ipm.RESPONSE_TAG) {
			this.asyncExecutor.responded(ipm.sequence(), ipm);
		} else if (ipm.tag() == Ipm.BIND_TAG) {
			this.asyncExecutor.execute(new BindProcessor(this, ipm));
		} else if (ipm.tag() == Ipm.PING_TAG) {
			this.asyncExecutor.execute(new PingProcessor(this, ipm));
		} else {

			for (IpmProcessor processor : ipmProcessors) {
				this.asyncExecutor.execute(() -> processor.process(this, ipm));
			}
		}
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
		if (this.pingTimedWorker != null) {
			pingTimedWorker.stop();
		}

		this.asyncExecutor.stop();
		this.asyncExecutor.clear();

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

	public synchronized void open() throws SocketClientCreateException, SessionException, InterruptedException,
			ExecutionException, TimeoutException {
		if (this.state != IpcSessionState.CLOSED) {
			log.debug("Session already open");
			return;
		}
		this.sequence = 0;
		this.socket = socketFactory.createSocket();

		this.senderProsumer.startWorkers();
		this.receiverWorker.start();
		this.asyncExecutor.start();

		if (this.pingTimedWorker != null) {
			pingTimedWorker.start();
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
		Future<Ipm> future = this.sendRequest(new Ipm().tag(Ipm.BIND_TAG).data(localAppId));
		try {
			Ipm response = future.get(timeout, TimeUnit.MILLISECONDS);
			if (response == null)
				throw new ExecutionException("bind failed. repsonse null", null);

			if (response.data() == null || response.data().length == 0)
				throw new ExecutionException("bind failed. repsonse " + response, null);

			this.remoteAppId = response.dataAsString();
			log.trace("bind as ={}", remoteAppId) ;

			setState(IpcSessionState.BOUND);

		} catch (Exception e) {
			setState(IpcSessionState.UNBOUND);
			throw new ExecutionException("bind failed.", e);
		}
	}

 
 

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

	public int getPendingResponses() {
		return asyncExecutor.size();
	}

	public int pendingJobs() {
		return asyncExecutor.pendingJobs();
	}

	public String getName() {
		return config().name() + "_" + config().appId() + "_" + socketFactory.toString();
	}

	public String sizeReport() {
//		return String.format(", null)
		return String.format("%d,%d,%d", asyncExecutor.size(), asyncExecutor.pendingJobs(),
				senderProsumer.pendingJobs());
	}

}