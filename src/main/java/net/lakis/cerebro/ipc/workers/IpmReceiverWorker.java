package net.lakis.cerebro.ipc.workers;

import java.io.IOException;

import net.lakis.cerebro.io.DataInputStream;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.Ipm;
import net.lakis.cerebro.jobs.Worker;

public class IpmReceiverWorker extends Worker {

	private IpcSession session;

	public IpmReceiverWorker(IpcSession session) {
		super(session.getName()+"_IpmReceiverWorker");
		this.session = session;
	}

	@Override
	public void work() throws Exception {

		try {
			if (session.isClosed()) {
				Thread.sleep(1000);
				return;
			}
			DataInputStream dis = session.socket().getInput();
			Ipm ipm = new Ipm();
			ipm.tracer(this.session.config().tracer());
			ipm.sessionName(this.session.toString());

			ipm.readFrom(dis);
			  

			this.session.handle(ipm);
		} catch (IOException ioe) {
			session.onIOException(ioe);
		}
	}

 

}