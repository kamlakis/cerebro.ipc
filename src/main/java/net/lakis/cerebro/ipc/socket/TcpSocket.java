package net.lakis.cerebro.ipc.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import lombok.Getter;
import net.lakis.cerebro.io.DataInputStream;
import net.lakis.cerebro.io.DataOutputStream;

public class TcpSocket implements Closeable {
	private @Getter Socket socket;
	private DataInputStream in;
	private DataOutputStream out;



	public TcpSocket(Socket socket) throws IOException {
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		this.socket = socket;
	}

//	public boolean d() {
//		if(this.socket == null)
//			return false;
// 	}

	public DataInputStream getInput() {
		return this.in;
	}

	public DataOutputStream getOutput() {
		return this.out;
	}

	@Override
	public void close() throws IOException {
		try {
			try {
				if (in != null)
					in.close();
			} finally {
				if (out != null)
					out.close();
			}

		} finally {
			if (socket != null)
				socket.close();
		}
	}

	@Override
	public String toString() {
		return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

	}

}
