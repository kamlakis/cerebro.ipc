package net.lakis.cerebro.ipc.ipm;

import java.io.IOException;

import lombok.Data;
import lombok.experimental.Accessors;
 
@Data
@Accessors(chain = true, fluent = true)
public class IpmEnquireLinkRequest implements Ipm {
	@Override
	public byte[] encode() throws IOException {
		return null;
	}

	@Override
	public void decode(byte[] data) throws IOException {
 	}

	@Override
	public IpmType type() {
		return IpmType.ENQUIRE_LINK_REQUEST;
	}

}
