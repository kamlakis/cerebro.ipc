package net.lakis.cerebro.ipc.workers;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.IpmEnquireLinkRequest;
import net.lakis.cerebro.jobs.TimedWorker;

@Log4j2
public class EnquireLinkTimedWorker extends TimedWorker {
	private IpcSession session;
	private boolean responded;

	public EnquireLinkTimedWorker(IpcSession session, long period) {
		super(session.getName() + "_EnquireLinkTimedWorker", period, period, period);
		this.session = session;
	}

	@Override
	public void work() throws Exception {
		if (System.currentTimeMillis() - session.lastActivity() > this.getPeriod()) {

			this.responded = false;
			session.send(new IpmEnquireLinkRequest());
			try {
				synchronized (this) {

					this.wait(session.getTimeout());
				}
				if (!this.responded) {
					throw new Exception("Enquire link timeout");
				}
			} catch (Exception e) {
				String remote = session.socketFactory().toString();
				log.error("Exception while Enquire link " + remote + ": ", e);
				session.close();
			}
		}
	}

	public void responded() {
		this.responded = true;
		synchronized (this) {
			this.notifyAll();
		}
	}

}
