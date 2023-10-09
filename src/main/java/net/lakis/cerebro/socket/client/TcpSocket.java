package net.lakis.cerebro.socket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import lombok.Getter;
import net.lakis.cerebro.lang.Strings;
import net.lakis.cerebro.socket.exceptions.SocketClientCreateException;
 
public class TcpSocket extends AbstractSocket {
	private @Getter Socket socket;

	public static ISocket createTcpClient(String host, int port) throws SocketClientCreateException  {
		Socket socket = new Socket();
		try {
			if (Strings.isBlank(host) || "0.0.0.0".equals(host))
				host = "127.0.0.1";
			InetSocketAddress address = null;
			address = new InetSocketAddress(host, port);
			socket.connect(address);
			return new TcpSocket(socket);
		} catch (Exception e) {
			try {
				socket.close();
			} catch (Exception e1) {
			}
			throw new SocketClientCreateException(e);
		}
	}

	public TcpSocket(Socket socket) throws IOException {
		super(socket.getInputStream(), socket.getOutputStream());
		this.socket = socket;
	}

//	public boolean d() {
//		if(this.socket == null)
//			return false;
// 	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
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
