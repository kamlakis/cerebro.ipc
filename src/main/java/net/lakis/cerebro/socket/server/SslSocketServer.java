package net.lakis.cerebro.socket.server;

import javax.net.ssl.SSLServerSocketFactory;

import net.lakis.cerebro.socket.SSLFactory;
import net.lakis.cerebro.socket.config.SocketConfig;
import net.lakis.cerebro.socket.exceptions.SocketServerCreateException;

public class SslSocketServer extends TcpSocketServer {

	public static SslSocketServer createServer(SocketConfig config) throws SocketServerCreateException {
		try {
			SSLFactory sslFactory = new SSLFactory(config.getSsl());
			SSLServerSocketFactory ssf = sslFactory.getServerSocketFactory();

			SslSocketServer server = new SslSocketServer();
			server.serverSocket = ssf.createServerSocket();
			server.bind(config);
			return server;
		} catch (Exception e) {
			throw new SocketServerCreateException(e);
		}
	}

}
