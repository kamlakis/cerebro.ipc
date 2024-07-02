package net.lakis.cerebro.ipc.processors;

import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.ipc.ipm.Ipm;

public interface IpmProcessor {
 
	/**
	 * Processor of <code>ipm</code> in a scope of <code>session</code>.
	 *
	 * @param ipm     IpmEvent to process
	 * @param session Session in which command was received
	 */
	public void process(IpcSession session, Ipm ipm);

	 
}
