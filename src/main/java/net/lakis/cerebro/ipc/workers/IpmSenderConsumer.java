package net.lakis.cerebro.ipc.workers;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.io.ByteArrayOutputStream;
import net.lakis.cerebro.io.DataOutputStream;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.Ipm;
import net.lakis.cerebro.jobs.prosumer.consumer.units.SingleConsumer;
import net.lakis.cerebro.lang.Hex;

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

		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] data = ipm.encode();
			
			baos.writeByte(ipm.type().getId());
			if(data != null)
				baos.writePBytes(data);
			
			
			data = baos.toByteArray();
						
			DataOutputStream dos = session.socket().getOutput();
			dos.writeBytes(data);
			dos.flush();

		} catch (IOException ioe) {
			session.onIOException(ioe);
		} catch (Exception e) {
			log.error("Exception ",e);
			throw e;
		}
	}

	 

}
