package net.lakis.cerebro.socket.client;

import java.io.Closeable;

import net.lakis.cerebro.io.DataInputStream;
import net.lakis.cerebro.io.DataOutputStream;

public interface ISocket extends Closeable {
	public DataInputStream getInput();
	public DataOutputStream getOutput();
}
