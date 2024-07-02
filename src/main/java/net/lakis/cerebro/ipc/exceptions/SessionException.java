package net.lakis.cerebro.ipc.exceptions;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;

/**
 * SessionException is throwed by Session's methods if commands cannot be
 * executed.
 */
public class SessionException extends Exception {
	private static final long serialVersionUID = -2801747496814531936L;
	private @Getter Short status;

	public SessionException() {
		super();
	}

	public SessionException(String message) {
		this(message, null);
	}

	public SessionException(Short status) {
		this(null, status);
	}

	public SessionException(String message, Short status) {
		super(message);
		this.status = status;
	}

	@Override
	public String getMessage() {
		String result = super.getMessage();
		if (status == null)
			return result;

		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(result)) {
			sb.append(result);
			sb.append(' ');
		}
		sb.append(status);
		return sb.toString();
	}

 
}
