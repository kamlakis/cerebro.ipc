package net.lakis.cerebro.ipc.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.lakis.cerebro.socket.config.SocketConfig;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
@ToString
public class IpcServerConfig extends IpcConfig {
	private SocketConfig socket;

}
