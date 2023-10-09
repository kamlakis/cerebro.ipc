package net.lakis.cerebro.ipc.ipm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Data;
import lombok.experimental.Accessors;
import net.lakis.cerebro.io.ByteArrayInputStream;
import net.lakis.cerebro.io.ByteArrayOutputStream;
import net.lakis.cerebro.ipc.IpcSession;
import net.lakis.cerebro.lang.Strings;

@Data
@Accessors(chain = true, fluent = true)
public class IpmData implements Ipm {

	private Map<String, String> headers;
	private byte[] data;
	private IpcSession session;

	public byte[] encode() throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> e : headers.entrySet()) {
					if (Strings.isNotBlank(e.getKey()) && Strings.isNotBlank(e.getValue())) {
						baos.writeCString(e.getKey());
						baos.writeCString(e.getValue());
					}
				}
			}
			baos.writeCString(null);
			if (data != null && data.length > 0)
				baos.writeBytes(data);

			return baos.toByteArray();
		}
	}

	public void decode(byte[] data) throws IOException {
		if (headers == null) {
			headers = new HashMap<String, String>();
		} else {
			headers.clear();
		}
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			for (;;) {
				String key = bais.readCString();
				if (Strings.isEmpty(key))
					break;
				headers.put(key, bais.readCString());
			}
			this.data = bais.readFullyTillTheEnd();

		}

	}

	@Override
	public IpmType type() {
		return IpmType.DATA;
	}

}
