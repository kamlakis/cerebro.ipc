package net.lakis.cerebro.ipc.workers;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.io.DataOutputStream;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.Ipm;
import net.lakis.cerebro.jobs.prosumer.consumer.units.SingleConsumer;

@Log4j2
public class IpmSenderConsumer extends SingleConsumer<Ipm> {

	private IpcSession session;

	public IpmSenderConsumer(IpcSession session) {
		this.session = session;
	}

	@Override
	public void handle(Ipm ipm) {
		if (session.isClosed())
			return;

		try {
			ipm.tracer(this.session.config().tracer());
			ipm.sessionName(this.session.toString());
			
			DataOutputStream dos = session.socket().getOutput();
			ipm.writeTo(dos);
			dos.flush();
		} catch (IOException ioe) {
			session.onIOException(ioe);
		} catch (Exception e) {
			log.error("Exception ", e);
			throw e;
		}
	}

}
