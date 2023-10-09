package net.lakis.cerebro.ipc.workers;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.IpcClientSessionsPool;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.IpcSessionState;
import net.lakis.cerebro.jobs.TimedWorker;

@Log4j2
public class HealthcheckTimedWorker extends TimedWorker {

	private IpcClientSessionsPool pool;

	public HealthcheckTimedWorker(IpcClientSessionsPool pool, long period) {
		super(pool.config().name()+"_HealthcheckTimedWorker", 0, period, period);
		this.pool = pool;

	}

	@Override
	public void work() throws Exception {
		this.check();
	}

	public void check() {
		for (IpcSession session : pool.getAllSessions()) {
			this.check(session);
		}
	}

	private void check(IpcSession session) {
		try {
			if (session.state() == IpcSessionState.CLOSED) {
				session.open();
			}

			if (session.state() != IpcSessionState.BOUND) {
				session.bind();
			}
		} catch (Exception e) {
			String remote = session.socketFactory().toString();
			log.error("Exception while checking " + remote + ": ", e);
		}
	}

}
