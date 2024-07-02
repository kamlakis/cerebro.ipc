package net.lakis.cerebro.ipc.event;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.IpcSessionState;
import net.lakis.cerebro.ipc.IpcSessionStateListener;

@Log4j2
public class SessionStateChangeEvent implements Runnable {
	private IpcSession session;
	private IpcSessionState previousState;
	private IpcSessionState currentState;

	public SessionStateChangeEvent(IpcSession session, IpcSessionState previousState,
			IpcSessionState currentState) {
		super();
		this.session = session;
		this.previousState = previousState;
		this.currentState = currentState;
	}

	@Override
	public void run() {
		try {
			for (IpcSessionStateListener listener : session.sessionStateListeners()) {
				listener.stateChanged(session, previousState, currentState);
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
		}
	}

}
