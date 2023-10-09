package net.lakis.cerebro.socket.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SslConfig {
	private String algorithm;
	private String storePath;
	private String storePassword;
	private String storeType;
}
