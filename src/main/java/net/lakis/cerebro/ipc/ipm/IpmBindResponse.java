package net.lakis.cerebro.ipc.ipm;

import java.io.IOException;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class IpmBindResponse implements Ipm {
	private String appId;
	@Override
	public byte[] encode() throws IOException {
		return appId.getBytes();
	}

	@Override
	public void decode(byte[] data) throws IOException {
		this.appId = new String(data);
	}

	@Override
	public IpmType type() {
		return IpmType.BIND_RESPONSE;
	}

}
