package net.lakis.cerebro.ipc.processors;

import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.IpcSessionState;
import net.lakis.cerebro.ipc.ipm.Ipm;

public class BindProcessor implements Runnable {

	private IpcSession ipcSession;
	private Ipm ipm;

	public BindProcessor(IpcSession ipcSession, Ipm ipm) {
		this.ipcSession = ipcSession;
		this.ipm = ipm;
	}

	@Override
	public void run() {

		Ipm response = ipm.createResponse();
		ipcSession.remoteAppId(ipm.dataAsString());
		ipcSession.setState(IpcSessionState.BOUND);
		if (ipcSession.state() == IpcSessionState.BOUND) {
			response.data(ipcSession.config().appId());
		}
		
		ipcSession.send(response);
	}

}
