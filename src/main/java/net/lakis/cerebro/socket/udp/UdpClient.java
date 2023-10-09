package net.lakis.cerebro.socket.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import lombok.extern.log4j.Log4j2;
import net.lakis.cerebro.lang.Strings;
import net.lakis.cerebro.lang.Strings.Arguments;

@Log4j2
public class UdpClient implements Closeable {

	private DatagramSocket socket;
	private InetAddress host;
	private int port;

	public UdpClient(String remoteAddress) throws SocketException, UnknownHostException {
		if (Strings.isNotBlank(remoteAddress)) {
			this.socket = new DatagramSocket();

			Arguments args = Strings.split(':', remoteAddress);
			this.host = InetAddress.getByName(args.getString());
			this.port = args.getInt();
			if (port == 0)
				port = 9000;
		}
	}

	public UdpClient(String host, int port) throws SocketException, UnknownHostException {
		this(InetAddress.getByName(host), port);
	}

	public UdpClient(InetAddress host, int port) throws SocketException, UnknownHostException {
		this.socket = new DatagramSocket();
		this.host = host;
		this.port = port;
		if (port == 0)
			port = 9000;
	}

	public void sendBytes(byte[] data) {
		this.sendBytes(data, data.length);
	}

	private void sendBytes(byte[] data, int length) {
		if (socket == null)
			return;
		
		try {
			DatagramPacket packet = new DatagramPacket(data, length, host, port);
			socket.send(packet);
		} catch (Exception e) {
			log.error("Exception: ", e);
		}
	}

	public boolean isOpen() {
		return this.socket != null;
	}
	@Override
	public void close() throws IOException {
		if (socket != null) {
			this.socket.close();
			this.socket = null;
		}

	}
}
