package net.lakis.cerebro.ipc.workers;

import net.lakis.cerebro.ipc.IpcServerSessionsPool;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.jobs.Worker;
 
public class AcceptSessionsWorker extends Worker {

	private IpcServerSessionsPool ipcServerSessionsPool;

	public AcceptSessionsWorker(IpcServerSessionsPool ipcServerSessionsPool) {
		super(ipcServerSessionsPool.config().name()+"_AcceptSessionsWorker");
		this.ipcServerSessionsPool = ipcServerSessionsPool;
	}

	@Override
	public void work() throws Exception {
		IpcSession session = this.ipcServerSessionsPool.createSession();
		session.open();
		this.ipcServerSessionsPool.add(session);
	}
}
