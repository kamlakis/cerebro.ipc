package net.lakis.cerebro.ipc.socket;

import java.net.InetSocketAddress;
import java.net.Socket;

import net.lakis.cerebro.ipc.socket.exceptions.SocketClientCreateException;
import net.lakis.cerebro.lang.Strings;

public class SocketFactory {

	private TcpSocketServer socketServer;
	private String host;
	private int port;
	private int timeout;

	public SocketFactory(String host, int port, int timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;

	}

	public SocketFactory(TcpSocketServer socketServer) {
		this.socketServer = socketServer;
	}

	public TcpSocket createSocket() throws SocketClientCreateException {
		if (this.socketServer != null)
			return this.socketServer.createSocket();

		Socket socket = new Socket();
		try {
			if (Strings.isBlank(host) || "0.0.0.0".equals(host))
				host = "127.0.0.1";
			InetSocketAddress address = null;
			address = new InetSocketAddress(host, port);
			if (timeout > 0)
				socket.connect(address, timeout);
			else
				socket.connect(address);
			socket.setKeepAlive(true);

			return new TcpSocket(socket);
		} catch (Exception e) {
			try {
				socket.close();
			} catch (Exception e1) {
			}
			throw new SocketClientCreateException(e);
		}

	}

	@Override
	public String toString() {
		if (socketServer != null)
			return socketServer.toString();
		return host + ":" + port;

	}

}
