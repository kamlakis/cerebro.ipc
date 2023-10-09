package net.lakis.cerebro.ipc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.config.IpcClientConfig;
import net.lakis.cerebro.ipc.config.IpcSocketConfig;
import net.lakis.cerebro.ipc.workers.HealthcheckTimedWorker;
import net.lakis.cerebro.socket.SocketFactory;
import net.lakis.cerebro.socket.config.SocketConfig;

@Log4j2
public class IpcClientSessionsPool extends IpcSessionsPool implements IpcSessionStateListener {

	private HealthcheckTimedWorker healthcheckTimedWorker;

	public IpcClientSessionsPool(IpcClientConfig<?> config) {
		super(config);

		for (IpcSocketConfig<?> socketConfig : config.nodes()) {
			IpcSession session = this.createSession(socketConfig);
			session.setConfigData(socketConfig.getData());
			super.add(session);
		}

		this.healthcheckTimedWorker = new HealthcheckTimedWorker(this, config.healthcheckTimer());

	}

	@Override
	public void stateChanged(IpcSession session, IpcSessionState previousState, IpcSessionState currentState) {
		try {
			if (previousState == IpcSessionState.BOUND) {
				if (this.sessionsMap().get(session.remoteAppId()) == session)
					this.sessionsMap().remove(session.remoteAppId());
			}

			if (currentState == IpcSessionState.BOUND) {
				IpcSession previousSession = this.sessionsMap().put(session.remoteAppId(), session);
				if (previousSession != null && previousSession != session) {
					// previousSession.unbindAndClose();
					previousSession.close();
				}
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
		}
	}

	public synchronized void open() {
		if (this.state() != IpcSessionState.CLOSED) {
			log.debug("Session pool already open");
			return;
		}
		if (this.healthcheckTimedWorker.getPeriod() > 0)
			this.healthcheckTimedWorker.start();
		else
			this.healthcheckTimedWorker.check();

		setState(IpcSessionState.OPEN);
	}

	public synchronized void close() throws InterruptedException, ExecutionException, TimeoutException {
		if (this.state() == IpcSessionState.CLOSED) {
			log.debug("Session pool already closed");
			return;
		}
		if (this.healthcheckTimedWorker.getPeriod() > 0)
			this.healthcheckTimedWorker.stop();

		for (IpcSession session : getAllSessions()) {
//			session.unbindAndClose();
			session.close();
		}
		setState(IpcSessionState.CLOSED);

		log.debug("socket pool closed");
	}

	public IpcSession createSession(SocketConfig socketConfig) {
		return super.createSession(new SocketFactory(socketConfig));
	}
	
	
}
