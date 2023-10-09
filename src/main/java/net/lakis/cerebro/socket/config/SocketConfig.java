package net.lakis.cerebro.socket.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SocketConfig {
	private String type = "TCP";
	private String host = "0.0.0.0";
	private int port = 12345;
	private int connectTimeout = 0;
	private int readTimeout = 0;
	private boolean reuseAddress = true;
	private boolean keepAlive = true;

	private SslConfig ssl;

	public String getSocketType() {
		if ("TCP".equalsIgnoreCase(type))
			return "TCP";
		else if ("TLS".equalsIgnoreCase(type) || "SSL".equalsIgnoreCase(type))
			return "SSL";
		else if (ssl != null)
			return "SSL";
		else
			return "TCP";
	}
}
