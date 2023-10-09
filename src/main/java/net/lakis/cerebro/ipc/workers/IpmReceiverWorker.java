package net.lakis.cerebro.ipc.workers;

import java.io.IOException;

import net.lakis.cerebro.io.DataInputStream;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.IpmBindRequest;
import net.lakis.cerebro.ipc.ipm.IpmBindResponse;
import net.lakis.cerebro.ipc.ipm.IpmData;
import net.lakis.cerebro.ipc.ipm.IpmType;
import net.lakis.cerebro.jobs.Worker;

public class IpmReceiverWorker extends Worker {

	private IpcSession session;

	public IpmReceiverWorker(IpcSession session) {
		super(session.getName() + "_IpmReceiverWorker");
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

			IpmType type = IpmType.get(dis.readByte());
			if (type == null)
				return;

			switch (type) {
			case BIND_REQUEST: {
				IpmBindRequest ipm = new IpmBindRequest();

				ipm.decode(dis.readPBytes());
				session.handleBindRequest(ipm);
				break;
			}
			case BIND_RESPONSE: {
				IpmBindResponse ipm = new IpmBindResponse();
				ipm.decode(dis.readPBytes());
				session.handleBindResponse(ipm);
				break;
			}
			case DATA: {
				IpmData ipm = new IpmData().session(session);
				ipm.decode(dis.readPBytes());
				session.handleData(ipm);
				break;
			}
			case ENQUIRE_LINK_REQUEST:
				session.handleEnquireLinkRequest();
				break;
			case ENQUIRE_LINK_RESPONSE:
				session.handleEnquireLinkResponse();
				break;
			default:
				break;

			}
		} catch (IOException ioe) {
			session.onIOException(ioe);
		}
	}
}