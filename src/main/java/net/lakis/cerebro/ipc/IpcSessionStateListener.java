package net.lakis.cerebro.ipc;

public interface IpcSessionStateListener {

	public void stateChanged(IpcSession session, IpcSessionState previousState, IpcSessionState currentState) throws Exception;
}
