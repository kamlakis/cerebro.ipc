package net.lakis.cerebro.socket;

import net.lakis.cerebro.socket.config.SocketConfig;
import net.lakis.cerebro.socket.exceptions.SocketServerCreateException;
import net.lakis.cerebro.socket.server.ISocketServer;
import net.lakis.cerebro.socket.server.SslSocketServer;
import net.lakis.cerebro.socket.server.TcpSocketServer;

public class SocketServerFactory {
	private SocketConfig config;

	public SocketServerFactory(SocketConfig config) {
		this.config = config;
	}

	


	public ISocketServer createServer() throws SocketServerCreateException {
		switch (config.getSocketType()) {
		case "TCP":
			return TcpSocketServer.createServer(config);
		case "SSL":
			return SslSocketServer.createServer(config);
		}
		throw new SocketServerCreateException("Unknown server type: " + config.getSocketType());
	}

}
