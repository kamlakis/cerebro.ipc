package net.lakis.cerebro.ipc.ipm;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public enum IpmType {
	
	ENQUIRE_LINK_REQUEST((byte) 0), //
	ENQUIRE_LINK_RESPONSE((byte) 1), //
	BIND_REQUEST((byte) 2), //
	BIND_RESPONSE((byte) 3), //
	DATA((byte) 4);

	private @Getter byte id;
	private static final Map<Byte, IpmType> lookup = new HashMap<>();

	static {
		for (IpmType td : IpmType.values()) {
			lookup.put(td.getId(), td);
		}
	}


	public static IpmType get(byte id) {
		IpmType ret = lookup.get(id);
		if (ret == null)
			return null;
		return ret;
	}

	private IpmType(byte id) {
		this.id = id;
	}
}
