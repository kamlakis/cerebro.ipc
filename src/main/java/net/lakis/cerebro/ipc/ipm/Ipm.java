package net.lakis.cerebro.ipc.ipm;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.lakis.cerebro.io.ByteArrayOutputStream;
import net.lakis.cerebro.io.DataInputStream;
import net.lakis.cerebro.io.DataOutputStream;
import net.lakis.cerebro.lang.Hex;
import net.lakis.cerebro.lang.Numbers;
import net.lakis.cerebro.lang.Strings;

@Accessors(fluent = true, chain = true)
@ToString
public class Ipm {
	public static final int RESPONSE_TAG = Integer.MAX_VALUE - 1;
	public static final int BIND_TAG = Integer.MAX_VALUE - 2;
	public static final int PING_TAG = Integer.MAX_VALUE - 3;
	private @Getter @Setter int sequence;
	private @Getter @Setter int tag;
	private @Getter byte[] data;
	private @Getter @Setter String tracer;
	private @Getter @Setter String sessionName;

	public void writeTo(DataOutputStream dos) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.writeInt(sequence);
			baos.writeInt(tag);
			baos.writePBytes(data);
			trace(true, sequence, tag, data);

			dos.writeBytes(baos.toByteArray());

		}

	}

	private void trace(boolean tx, int sequence, int tag, byte[] data) {
		try {
			if (Strings.isBlank(tracer))
				return;
			String tagStr;
			switch (tag) {
			case BIND_TAG:
				tagStr = "bind";
				break;
			case PING_TAG:
				tagStr = "ping";
				break;
			case RESPONSE_TAG:
				tagStr = "response";
				
				break;
			default:
				tagStr = String.valueOf(tag);
				break;
			}
			String txrx = tx?"TX":"RX";

			String sequenceStr = "";
			if((tx && tag == RESPONSE_TAG) || (!tx && tag != RESPONSE_TAG)) {
				sequenceStr ="r"+ sequence;
			} else {
				sequenceStr ="l"+ sequence;
			}
			
			if (data != null && data.length > 0)
				LogManager.getLogger("tracer").trace("{} {} tag={} sequence={} data=\n{}", sessionName, txrx, tagStr,
						sequenceStr, Hex.dump(data));
			else
				LogManager.getLogger("tracer").trace("{} {} tag={} sequence={} no data", sessionName, txrx, tagStr,
						sequenceStr);

		} catch (Exception e) {
		}

	}

	public void readFrom(DataInputStream dis) throws IOException {
		sequence = dis.readInt();
		tag = dis.readInt();
		data = dis.readPBytes();
		trace(false, sequence, tag, data);

	}

	public Ipm data(byte[] v) {
		data = v;
		return this;
	}

	public Ipm data(byte v) {
		data = new byte[] { v };
		return this;
	}

	public Ipm data(boolean v) {
		data = new byte[] { (byte) (v ? 1 : 0) };
		return this;
	}

	public Ipm data(short v) {
		data = Numbers.of(v).getBytes();
		return this;
	}

	public Ipm data(int v) {
		data = Numbers.of(v).getBytes();
		return this;
	}

	public Ipm data(long v) {
		data = Numbers.of(v).getBytes();
		return this;
	}

	public Ipm data(String s) {
		data = s.getBytes();
		return this;
	}

	public byte dataAsByte() {
		return data[0];
	}

	public boolean dataAsBoolean() {
		return data[0] == 1;
	}

	public short dataAsShort() {
		return Numbers.of(data).getShort();
	}

	public int dataAsInt() {
		return Numbers.of(data).getInt();
	}

	public long dataAsLong() {
		return Numbers.of(data).getLong();
	}

	public String dataAsString() {
		return new String(data);
	}

	public Ipm createResponse() {
		Ipm ipm = new Ipm();
		ipm.tag = RESPONSE_TAG;
		ipm.sequence = this.sequence;
		return ipm;
	}

}