package net.lakis.cerebro.ipc.socket.exceptions;

public class SocketClientCreateException extends Exception {
	private static final long serialVersionUID = -5842279377749805804L;

	public SocketClientCreateException(Exception ex) {
		super(ex);
	}

	public SocketClientCreateException(String message) {
		super(message);
	}
}
