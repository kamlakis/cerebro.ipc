package net.lakis.cerebro.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;

import net.lakis.cerebro.socket.config.SslConfig;

 
public class SSLFactory {
	/*
	 * 
	 * 1 - Create a private key and public certificate using the following command :
	 * 
	 * openssl req -newkey rsa:2048 -x509 -keyout private.key -out public.crt -days
	 * 3650 -nodes
	 * 
	 * 2 - Create a PKCS12 keystore :
	 * 
	 * openssl pkcs12 -export -in public.crt -inkey private.key -out keystore.pkcs12
	 * 
	 * 3 - Create a truststore :
	 * 
	 * keytool -import -file public.crt -keystore truststore.jks
	 * 
	 * 4 - On server side use the following :
	 * 
	 * SSLFactory sslFactory = new SSLFactory("H:/tls/keystore.pkcs12", "password",
	 * SSLFactory.FACTORY_TYPE_PKCS12);
	 * 
	 * SSLServerSocketFactory ssf = sslFactory.getServerSocketFactory();
	 * 
	 * ServerSocket serverSocket = ssf.createServerSocket(port);
	 * 
	 * 5 - On client side use the following :
	 * 
	 * SSLFactory sslFactory = new SSLFactory("H:/tls/truststore.jks", "password",
	 * SSLFactory.FACTORY_TYPE_JKS);
	 * 
	 * SSLSocketFactory ssf = sslFactory.getClientSocketFactory();
	 * 
	 * clientSocket = ssf.createSocket(ip, port);
	 * 
	 */
	public static final String FACTORY_TYPE_JKS = "jks";
	public static final String FACTORY_TYPE_PKCS12 = "pkcs12";
	private SSLContext context;

	public SSLFactory(SslConfig config) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
			FileNotFoundException, NoSuchAlgorithmException, CertificateException, IOException {
		this(config.getAlgorithm(), config.getStorePath(), config.getStorePassword(), config.getStoreType());
	}

	public SSLFactory(String algorithm) throws KeyManagementException, NoSuchAlgorithmException {
		this.trustAllCertificates(algorithm);
	}

	public SSLFactory(String algorithm, String path, String password, String type)
			throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException, KeyManagementException {
		if (StringUtils.isNotBlank(path))
			this.loadKeyStore(algorithm, path, password, type);
		else
			this.trustAllCertificates(algorithm);
	}

	public void trustAllCertificates(String algorithm) throws NoSuchAlgorithmException, KeyManagementException {
		X509TrustManager trustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
		TrustManager[] trustAllCerts = { trustManager };
		this.context = SSLContext.getInstance(algorithm);
		this.context.init(null, trustAllCerts, null);
	}

	public void loadKeyStore(String algorithm, String path, String password, String type)
			throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException, KeyManagementException {
		KeyStore keystore = KeyStore.getInstance(type);
		try (InputStream keystoreStream = new FileInputStream( new File(path))) {
			keystore.load(keystoreStream, password.toCharArray());
		}

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, password.toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keystore);

		this.context = SSLContext.getInstance(algorithm);
		this.context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
	}

	public static void setDefaultTrustStore(String path, String password, String type) {
		System.setProperty("javax.net.ssl.trustStore", path.replace('\\', '/'));
		System.setProperty("javax.net.ssl.trustStorePassword", password);
		System.setProperty("javax.net.ssl.trustStoreType", type);
	}

	public static void setDefaultKeyStore(String path, String password, String type) {
		System.setProperty("javax.net.ssl.keyStore", path = path.replace('\\', '/'));
		System.setProperty("javax.net.ssl.keyStorePassword", password);
		System.setProperty("javax.net.ssl.keyStoreType", type);
	}

	public static SSLServerSocketFactory getDefaultServerSocketFactory() {
		return (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	}

	public static SSLSocketFactory getDefaultClientSocketFactory() {
		return (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	public SSLServerSocketFactory getServerSocketFactory() {
		return context.getServerSocketFactory();
	}

	public SSLSocketFactory getClientSocketFactory() {
		return context.getSocketFactory();
	}

}
