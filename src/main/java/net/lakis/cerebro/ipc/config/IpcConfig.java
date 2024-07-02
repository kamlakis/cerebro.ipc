package net.lakis.cerebro.ipc.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
@ToString
public class IpcConfig {
	private String appId;
	private boolean  closeWhenUnbound = true;
	private int incomingThreads = 1;
	private int timeout = 20 * 1000;
	private int pingTimer;
	private String name;
	private String tracer;
}
