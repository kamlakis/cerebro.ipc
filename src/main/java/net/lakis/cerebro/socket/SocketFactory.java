package net.lakis.cerebro.socket;

import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import org.apache.commons.lang.StringUtils;

import net.lakis.cerebro.socket.client.ISocket;
import net.lakis.cerebro.socket.client.TcpSocket;
import net.lakis.cerebro.socket.config.SocketConfig;
import net.lakis.cerebro.socket.exceptions.SocketClientCreateException;
import net.lakis.cerebro.socket.server.ISocketServer;


public class SocketFactory {

	private SocketConfig config;
	private ISocketServer socketServer;

	public SocketFactory(SocketConfig config) {
		this.config = config;
	}

	
 

	public SocketFactory(ISocketServer socketServer) {
		this.socketServer = socketServer;
	}

	public ISocket createSocket() throws SocketClientCreateException {
		if (this.socketServer != null)
			return this.createServerSocket();
		else
			return this.createClientSocket();

	}

	private ISocket createServerSocket() throws SocketClientCreateException {
		return this.socketServer.createSocket();
	}

	private ISocket createClientSocket() throws SocketClientCreateException {
		switch (config.getSocketType()) {
		case "TCP":
			return this.createTcpClientSocket();
		case "SSL":
			return this.createSslClientSocket();
		}
		throw new SocketClientCreateException("Unknown client type: " + config.getSocketType());
	}

	private ISocket createSslClientSocket() throws SocketClientCreateException {
		try {
			SSLFactory sslFactory = new SSLFactory(config.getSsl());
			Socket socket = sslFactory.getClientSocketFactory().createSocket();

			InetSocketAddress address = null;
			if (StringUtils.isBlank(config.getHost()) || config.getHost().equals("0.0.0.0"))
				address = new InetSocketAddress("127.0.0.1", config.getPort());
			else
				address = new InetSocketAddress(config.getHost(), config.getPort());

			if (config.getConnectTimeout() > 0) {
				socket.setSoTimeout(config.getConnectTimeout());
				socket.connect(address, config.getConnectTimeout());
			} else {
				socket.connect(address);
			}

			((SSLSocket) socket).startHandshake();

			if (config.getReadTimeout() > 0)
				socket.setSoTimeout(config.getReadTimeout());
			else if (config.getConnectTimeout() > 0)
				socket.setSoTimeout(0);

			return new TcpSocket(socket);
		} catch (Exception e) {
			throw new SocketClientCreateException(e);
		}
	}

	private ISocket createTcpClientSocket() throws SocketClientCreateException {
		try {
			Socket socket = new Socket();

			InetSocketAddress address = null;
			if (StringUtils.isBlank(config.getHost()) || config.getHost().equals("0.0.0.0"))
				address = new InetSocketAddress("127.0.0.1", config.getPort());
			else
				address = new InetSocketAddress(config.getHost(), config.getPort());

			if (config.getConnectTimeout() > 0)
				socket.connect(address, config.getConnectTimeout());
			else
				socket.connect(address);

			if (config.getReadTimeout() > 0)
				socket.setSoTimeout(config.getReadTimeout());
			if (config.isKeepAlive())
				socket.setKeepAlive(true);

			return new TcpSocket(socket);
		} catch (Exception e) {
			throw new SocketClientCreateException(e);
		}
	}

 
	@Override
	public String toString() {
		if (config != null)
			return config.getHost() + ":" + config.getPort();
		if (socketServer != null)
			return socketServer.toString();
		return null;
	}

}
