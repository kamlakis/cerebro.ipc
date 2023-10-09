package net.lakis.cerebro.ipc.ipm;

import java.io.IOException;

public interface Ipm {

	public IpmType type();

	public byte[] encode() throws IOException;

	public void decode(byte[] data) throws IOException;

}
