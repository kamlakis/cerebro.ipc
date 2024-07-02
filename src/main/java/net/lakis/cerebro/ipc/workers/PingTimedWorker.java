package net.lakis.cerebro.ipc.workers;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.Ipm;
import net.lakis.cerebro.jobs.TimedWorker;

@Log4j2
public class PingTimedWorker extends TimedWorker {
	private IpcSession session;

	public PingTimedWorker(IpcSession session, long period) {
		super(session.getName() + "_PingTimedWorker", period, period, period);
		this.session = session;
	}

	@Override
	public void work() throws Exception {
		if (System.currentTimeMillis() - session.lastActivity() > this.getPeriod()) {
			Future<Ipm> future = this.session.sendRequest(new Ipm().tag(Ipm.PING_TAG));

			try {
				Ipm response = future.get(session.getTimeout(), TimeUnit.MILLISECONDS);
				if (response == null) {
					log.info("Ping null on {}", session.getName());
					throw new Exception("Ping null");
				} else {
					log.trace("Ping Response received on {}", session.getName());
				}
			} catch (Exception e) {
				String remote = session.socketFactory().toString();
				log.error("Exception while Ping " + remote + ": ", e);
				session.close();
			}
		}
	}

}
