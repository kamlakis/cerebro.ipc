package net.lakis.cerebro.socket.exceptions;

public class SocketServerCreateException extends Exception {
	private static final long serialVersionUID = 398856853684800776L;

	public SocketServerCreateException(Exception ex) {
		super(ex);
	}

	public SocketServerCreateException(String message) {
		super(message);
	}
}
