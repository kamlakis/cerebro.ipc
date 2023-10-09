package net.lakis.cerebro.ipc.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.lakis.cerebro.socket.config.SocketConfig;
 
@Getter
@Setter
@ToString
public class IpcSocketConfig<T> extends SocketConfig {

	T data;
}
