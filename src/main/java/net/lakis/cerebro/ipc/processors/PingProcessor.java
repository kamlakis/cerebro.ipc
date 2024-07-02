package net.lakis.cerebro.ipc.processors;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.Ipm;

/**
 * EnquireLinkProcessor processes enquire link requests and responds with
 * enquire_link_resp IPM. Add an instance of EnquireLink processor to Session if
 * you want to process enquire_link requests.
 */
@Log4j2
public class PingProcessor  implements Runnable {

	private IpcSession ipcSession;
	private Ipm ipm;

	public PingProcessor(IpcSession ipcSession, Ipm ipm) {
		this.ipcSession = ipcSession;
		this.ipm = ipm;
	}

	@Override
	public void run() {
		log.trace("ping recieved. sending response");
		ipcSession.send(ipm.createResponse());
	}

}
 
