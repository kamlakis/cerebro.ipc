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
 	private int timeout = 20 * 1000;
	private int enquireLinkTimer;
	private String name;
 }
