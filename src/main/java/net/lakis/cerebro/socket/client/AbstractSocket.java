package net.lakis.cerebro.socket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.lakis.cerebro.io.DataInputStream;
import net.lakis.cerebro.io.DataOutputStream;


public abstract class AbstractSocket implements ISocket {
	private DataInputStream in;
	private DataOutputStream out;

	public AbstractSocket(InputStream inputStream, OutputStream outputStream) throws IOException {
		this.in = new DataInputStream(inputStream);
		this.out = new DataOutputStream(outputStream);
	}

	@Override
	public DataInputStream getInput() {
		return this.in;
	}

	@Override
	public DataOutputStream getOutput() {
		return this.out;
	}

	@Override
	public void close() throws IOException {
		try {
			if (in != null)
				in.close();
		} finally {
			if (out != null)
				out.close();
		}
	}

}
