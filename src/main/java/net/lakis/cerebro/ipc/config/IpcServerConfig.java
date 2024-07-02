package net.lakis.cerebro.ipc.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
@ToString
public class IpcServerConfig extends IpcConfig {
	private String host;
	private int port;

}
