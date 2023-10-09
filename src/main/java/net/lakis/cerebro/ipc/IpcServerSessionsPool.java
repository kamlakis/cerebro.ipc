package net.lakis.cerebro.ipc;

import java.io.IOException;
import java.util.Collection;

import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.config.IpcServerConfig;
import net.lakis.cerebro.ipc.workers.AcceptSessionsWorker;
import net.lakis.cerebro.jobs.Worker;
import net.lakis.cerebro.socket.SocketServerFactory;
import net.lakis.cerebro.socket.exceptions.SocketServerCreateException;
import net.lakis.cerebro.socket.server.ISocketServer;

@Accessors(fluent = true, chain = true)
@Log4j2
public class IpcServerSessionsPool extends IpcSessionsPool {
	private Worker acceptSessionsWorker;
	private SocketServerFactory socketFactory;
	private ISocketServer server;

	public IpcServerSessionsPool(IpcServerConfig config) {
		super(config);

		this.socketFactory = new SocketServerFactory(config.socket());
		this.acceptSessionsWorker = new AcceptSessionsWorker(this);

	}

	
	@Override
	public void stateChanged(IpcSession session, IpcSessionState previousState, IpcSessionState currentState) {
		try {
			if (previousState == IpcSessionState.BOUND) {
				if (this.sessionsMap().get(session.remoteAppId()) == session)
					this.sessionsMap().remove(session.remoteAppId());
			}

			if (currentState == IpcSessionState.CLOSED) {

				this.sessionsSet().remove(session);
			} else if (currentState == IpcSessionState.BOUND) {

				IpcSession previousSession = this.sessionsMap().put(session.remoteAppId(), session);
				if (previousSession != null && previousSession != session)
//					previousSession.unbindAndClose();
					previousSession.close();
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
		}
	}

	
	public Collection<IpcSession> getAllSessions() {
		
		Collection<IpcSession> sessionsSet = super.getAllSessions();
		sessionsSet.removeIf((session) -> {
			return session.socket() == null;
		});
		return sessionsSet;
	}
	
	
	public synchronized void open() throws SocketServerCreateException {
		if (this.state() != IpcSessionState.CLOSED) {
			log.debug("Session pool already open");
			return;
		}
		this.server = socketFactory.createServer();
		this.acceptSessionsWorker.start();

		setState(IpcSessionState.OPEN);
	}

	public synchronized void close() {
		if (this.state() == IpcSessionState.CLOSED) {
			log.debug("Session pool already closed");
			return;
		}
		setState(IpcSessionState.CLOSED);

		this.acceptSessionsWorker.stop();

		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				log.error("Closing server socket failed", e);
			} finally {
				server = null;
			}
		}

		log.debug("socket pool closed");
	}

	public IpcSession createSession() {
		return super.createSession(server.createSocketFactory());

	}

	
}
