package com.riddimon.pickpix.api;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.riddimon.pickpix.api.HttpUtils.TrustAllManager;

/**
 * Used in Froyo devices as HttpSUrlConnection is not properly implemented. Modified to use
 * custom hostname verifier in debug mode for accepting self-signed certificates.
 * @author ridhishguhan
 */
public class HttpManager {
	private DefaultHttpClient client;
	private static HttpManager instance = null;

	private HttpManager(Boolean debug, String version) {
		// Set basic data
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, HttpUtils.userAgent + "/" + version);

		// Make pool
		ConnPerRoute connPerRoute = new ConnPerRouteBean(12);
		ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
		ConnManagerParams.setMaxTotalConnections(params, 20);

		// Set timeout
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, 60 * 1000);
		HttpConnectionParams.setSoTimeout(params, 60 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		// Some client params
		HttpClientParams.setRedirecting(params, false);

		// Register http/s schemas!
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		if (debug) {
			// Install the all-trusting trust manager
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustManagers = new X509TrustManager[1];
			trustManagers[0] = new TrustAllManager();

			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustManagers, null);
				schReg.register(new Scheme("https", (SocketFactory) sc
						.getSocketFactory(), 443));
			} catch (Exception e) {
				;
			}
		} else {
			schReg.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));

		}
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);
		client = new DefaultHttpClient(conMgr, params);

	}

	private static HttpManager getInstance(Boolean debug, String version) {
		if (instance == null)
			instance = new HttpManager(debug, version);
		return instance;
	}

	public static HttpResponse execute(HttpUriRequest req, Boolean debug, String version)
			throws IOException {
		return getInstance(debug == null ? Boolean.FALSE : debug, version).client
				.execute(req);
	}
}