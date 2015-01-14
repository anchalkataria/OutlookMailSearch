package com.wk.mailsearch.service;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating MySchemeSocket objects.
 */
@SuppressWarnings("deprecation")
public class MySchemeSocketFactory implements SchemeSocketFactory {

	/* (non-Javadoc)
	 * @see org.apache.http.conn.scheme.SchemeSocketFactory#createSocket(org.apache.http.params.HttpParams)
	 */
	public Socket createSocket(final HttpParams params) throws IOException {
		if (params == null) {
			throw new IllegalArgumentException("HTTP parameters may not be null");
		}
		String proxyHost = (String) params.getParameter("socks.host");
		Integer proxyPort = (Integer) params.getParameter("socks.port");

		InetSocketAddress socksaddr = new InetSocketAddress(proxyHost, proxyPort);
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
		return new Socket(proxy);
	}

	/* (non-Javadoc)
	 * @see org.apache.http.conn.scheme.SchemeSocketFactory#connectSocket(java.net.Socket, java.net.InetSocketAddress, java.net.InetSocketAddress, org.apache.http.params.HttpParams)
	 */
	public Socket connectSocket(
			final Socket socket,
			final InetSocketAddress remoteAddress,
			final InetSocketAddress localAddress,
			final HttpParams params)
	throws IOException, UnknownHostException, ConnectTimeoutException {
		if (remoteAddress == null) {
			throw new IllegalArgumentException("Remote address may not be null");
		}
		if (params == null) {
			throw new IllegalArgumentException("HTTP parameters may not be null");
		}
		Socket sock;
		if (socket != null) {
			sock = socket;
		} else {
			sock = createSocket(params);
		}
		if (localAddress != null) {
			sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
			sock.bind(localAddress);
		}
		int timeout = HttpConnectionParams.getConnectionTimeout(params);
		try {
			sock.connect(remoteAddress, timeout);
		} catch (SocketTimeoutException ex) {
			throw new ConnectTimeoutException("Connect to " + remoteAddress.getHostName() + "/"
					+ remoteAddress.getAddress() + " timed out");
		}
		return sock;
	}

	/* (non-Javadoc)
	 * @see org.apache.http.conn.scheme.SchemeSocketFactory#isSecure(java.net.Socket)
	 */
	public boolean isSecure(Socket arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

}