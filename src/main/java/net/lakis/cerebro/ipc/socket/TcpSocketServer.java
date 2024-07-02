package net.lakis.cerebro.ipc.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Getter;
import net.lakis.cerebro.ipc.socket.exceptions.SocketClientCreateException;
import net.lakis.cerebro.ipc.socket.exceptions.SocketServerCreateException;
import net.lakis.cerebro.lang.Strings;

public class TcpSocketServer implements Closeable {
	protected ServerSocket serverSocket;
	private @Getter int port;
	private @Getter String host;

	public TcpSocketServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void bind() throws SocketServerCreateException {
		try {
			this.serverSocket = new ServerSocket();
			InetSocketAddress inetSocketAddress = null;
			if (Strings.isBlank(host) || host.equals("0.0.0.0"))
				inetSocketAddress = new InetSocketAddress(port);
			else
				inetSocketAddress = new InetSocketAddress(host, port);
			this.serverSocket.setReuseAddress(true);

			this.serverSocket.bind(inetSocketAddress);
		} catch (Exception e) {
			throw new SocketServerCreateException(e);
		}
	}

	public TcpSocket createSocket() throws SocketClientCreateException {
		try {
			Socket socket = this.serverSocket.accept();
			socket.setKeepAlive(true);
			return new TcpSocket(socket);
		} catch (Exception e) {
			throw new SocketClientCreateException(e);
		}
	}

	public SocketFactory createSocketFactory() {
		return new SocketFactory(this);
	}

	@Override
	public void close() throws IOException {
		this.serverSocket.close();
	}

	@Override
	public String toString() {
		return host + ":" + port;
	}
}
