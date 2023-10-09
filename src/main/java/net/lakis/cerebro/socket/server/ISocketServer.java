package net.lakis.cerebro.socket.server;

import java.io.Closeable;

import net.lakis.cerebro.socket.SocketFactory;
import net.lakis.cerebro.socket.client.ISocket;
import net.lakis.cerebro.socket.exceptions.SocketClientCreateException;

public interface ISocketServer extends Closeable {

	public ISocket createSocket() throws SocketClientCreateException;
	public SocketFactory createSocketFactory() ;

}
