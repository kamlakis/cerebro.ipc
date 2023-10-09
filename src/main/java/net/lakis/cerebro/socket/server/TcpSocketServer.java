package net.lakis.cerebro.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import net.lakis.cerebro.lang.Strings;
import net.lakis.cerebro.socket.SocketFactory;
import net.lakis.cerebro.socket.client.ISocket;
import net.lakis.cerebro.socket.client.TcpSocket;
import net.lakis.cerebro.socket.config.SocketConfig;
import net.lakis.cerebro.socket.exceptions.SocketClientCreateException;
import net.lakis.cerebro.socket.exceptions.SocketServerCreateException;

public class TcpSocketServer implements ISocketServer {
	protected ServerSocket serverSocket;
	private SocketConfig config;

	public static TcpSocketServer createServer(SocketConfig config) throws SocketServerCreateException {
		try {
			TcpSocketServer server = new TcpSocketServer();
			server.serverSocket = new ServerSocket();
			server.bind(config);
			return server;
		} catch (Exception e) {
			throw new SocketServerCreateException(e);
		}
	}

	public static TcpSocketServer createServer(String host, int port) throws SocketServerCreateException {
		SocketConfig config = new SocketConfig();
		config.setHost(host);
		config.setPort(port);
		return createServer(config);
	}

	public static TcpSocketServer createServer(int port) throws SocketServerCreateException {
		return createServer("0.0.0.0", port);
	}

	protected void bind(SocketConfig config) throws IOException {
		this.config = config;
		InetSocketAddress inetSocketAddress = null;
		if (Strings.isBlank(config.getHost()) || config.getHost().equals("0.0.0.0"))
			inetSocketAddress = new InetSocketAddress(config.getPort());
		else
			inetSocketAddress = new InetSocketAddress(config.getHost(), config.getPort());
		if (config.isReuseAddress())
			this.serverSocket.setReuseAddress(true);
		if (config.getReadTimeout() > 0)
			this.serverSocket.setSoTimeout(config.getReadTimeout());

		this.serverSocket.bind(inetSocketAddress);
	}

	@Override
	public ISocket createSocket() throws SocketClientCreateException {
		try {
			Socket socket = this.serverSocket.accept();
			if (config.isKeepAlive())
				socket.setKeepAlive(true);
			if (config.getReadTimeout() > 0)
				socket.setSoTimeout(config.getReadTimeout());
			return new TcpSocket(socket);
		} catch (Exception e) {
			throw new SocketClientCreateException(e);
		}
	}

	@Override
	public SocketFactory createSocketFactory() {
		return new SocketFactory(this);
	}

	@Override
	public void close() throws IOException {
		this.serverSocket.close();
	}

	@Override
	public String toString() {
		return config.getHost() + ":" + config.getPort();
	}
}
