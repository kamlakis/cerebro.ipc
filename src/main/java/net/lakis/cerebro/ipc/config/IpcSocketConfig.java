package net.lakis.cerebro.ipc.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IpcSocketConfig<T> {
	private String host;
	private int port;
	private int timeout;
	private T data;
}
