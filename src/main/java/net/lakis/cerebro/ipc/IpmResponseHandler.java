package net.lakis.cerebro.ipc;

import net.lakis.cerebro.ipc.ipm.Ipm;

public interface IpmResponseHandler  {
	public void onResponse(Ipm response, Exception ex);
}
