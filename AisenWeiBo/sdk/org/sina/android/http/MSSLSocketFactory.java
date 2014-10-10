package org.sina.android.http;

import java.io.IOException;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;

import java.net.UnknownHostException;

import java.security.KeyManagementException;

import java.security.KeyStore;

import java.security.KeyStoreException;

import java.security.NoSuchAlgorithmException;

import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import javax.net.ssl.TrustManager;

import javax.net.ssl.X509TrustManager;

import java.security.cert.CertificateException;

import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.SSLSocketFactory;

public class MSSLSocketFactory extends SSLSocketFactory {
	
	SSLContext sslContext = SSLContext.getInstance("TLS");

	public MSSLSocketFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {

		super(truststore);

		TrustManager tm = new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {

			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {

			}

			public X509Certificate[] getAcceptedIssuers() {

				return null;

			}

		};

		sslContext.init(null, new TrustManager[] { tm }, null);

	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		injectHostname(socket, host);
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);

	}

	/**
	 * 这个方法，是在不同的WIFI网络中报出org.apache.http.NoHttpResponseException: The target
	 * server failed to respond这个错误新增的
	 * 
	 * @param socket
	 * @param host
	 */
	private void injectHostname(Socket socket, String host) {
		try {
			Field field = InetAddress.class.getDeclaredField("hostName");
			field.setAccessible(true);
			field.set(socket.getInetAddress(), host);
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
	}

	@Override
	public Socket createSocket() throws IOException {

		return sslContext.getSocketFactory().createSocket();

	}

}
