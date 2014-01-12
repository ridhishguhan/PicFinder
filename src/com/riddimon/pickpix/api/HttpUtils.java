package com.riddimon.pickpix.api;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.riddimon.pickpix.util.Utils;

/**
 * Utility class for HTTP calls
 * @author ridhishguhan
 */
public class HttpUtils {
	public static final String userAgent = "kryptos/android";
	public enum HttpMethod {
		GET, POST, PUT, DELETE, INVALID
	}
	private static final Logger logger = LoggerFactory
			.getLogger(HttpUtils.class);

	private String version = "";
	private Context context;
	private static HttpUtils instance;

	private HttpUtils(Context context) {
		// private constructor to prevent instantiation
		this.context = context;
		try {
			// get version number to be set as part of user agent string
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {}
		if (Utils.DEV_ENV) {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			try {
				TrustManager[] trustManagers = new X509TrustManager[1];
				trustManagers[0] = new TrustAllManager();

				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustManagers, null);
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (Exception ex) {}
		}
		enableHttpResponseCache();
	}

	/**
	 * Use reflection to enable response cache for devices with ICS and above
	 */
	private void enableHttpResponseCache() {
	    try {
	        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
	        File httpCacheDir = new File(context.getCacheDir(), "http");
	        Class.forName("android.net.http.HttpResponseCache")
	            .getMethod("install", File.class, long.class)
	            .invoke(null, httpCacheDir, httpCacheSize);
	    } catch (Exception httpResponseCacheNotAvailable) {
	    }
	}

	private static HttpUtils getInstance(Context context) {
		if (instance == null)
			instance = new HttpUtils(context);
		return instance;
	}

	/**
	 * Used to refer to connection status while executing HTTP transactions
	 */
	public enum ConnectionStatus {
		BOTH_CONNECTED, WIFI_CONNECTED, DATA_CONNECTED, CONNECTING, NO_CONNECTION
	}

	/**
	 * Used for bypass problem with self-signed certificates in SSL connections
	 */
	public static class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)	throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}


	/**
	 * Uses ConnectivityManager API to check for connectivity
	 * @param context
	 * @return true || false
	 */
	public boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		// test for connection
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			logger.info("Connection available : <", cm.getActiveNetworkInfo().toString() + ">");
			return true;
		} else {
			// no connection
			logger.info("No connectivity.");
			return false;
		}
	}

	private static String getEncodedParameters(Map<String, String> params) {
		StringBuilder s = new StringBuilder();
		synchronized(params) {
			for (String key : params.keySet()) {
				if (s.length() != 0) {
					s.append("&");
				}
				String value = params.get(key).toString();
				String encodedValue = null;
				try {
					encodedValue = URLEncoder.encode(value, "utf8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					logger.warn("Could not encode URL");
				}
				if (!TextUtils.isEmpty(encodedValue)) {
					s.append(key).append("=").append(encodedValue);
				}

				//logger.trace("Encoding from: {}", value);
				//logger.trace("Encoding to  : {}", encodedValue);
			}
		}
		return s.toString();
	}

	/**
	 * Do an HTTP GET / DELETE / POST / PUT to the url with the parameters
	 * @param context the activity or service context. Preferably, the application context
	 * @param method the HttpMethod HttpMethod.GET / .POST/ .PUT / .DELETE
	 * @param url the url without the parameters appended
	 * @param paramz the parameters to be added to the GET/DELETE request
	 * @return <b>response</b> the string response
	 * TODO: throw appropriate exceptions to signal errors
	 * @throws IOException 
	 */
	public static String execute(Context context, HttpMethod method, String url, Map<String, String> paramz) throws IOException {
		HttpUtils instance = getInstance(context);
		return instance.execute(method, url, paramz);
	}

	private String execute(HttpMethod method, String url, Map<String, String> paramz) throws IOException {
		if (!(method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)
				|| method.equals(HttpMethod.PUT) || method.equals(HttpMethod.POST)) || TextUtils.isEmpty(url)) {
			logger.error("Invalid request : {} | {}", method.name(), url);
			return null;
		}
		logger.trace("HTTP {} : {}", method.name(), url);
		BufferedInputStream bis = null;
		StringBuilder builder = new StringBuilder();
		int buf_size = 50 * 1024; // read in chunks of 50 KB
		ByteArrayBuffer bab = new ByteArrayBuffer(buf_size);
		String query = getEncodedParameters(paramz);
		logger.trace("Query String : {}", query);
		HttpURLConnection conn = null;
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				HttpUriRequest req = null;
				switch (method) {
				case GET:
				case DELETE:
					url = paramz.size() > 0 ? url + "?" + query : url;
					if (method.equals(HttpMethod.GET)) {
						req = new HttpGet(url);
					} else if (method.equals(HttpMethod.DELETE)){
						req = new HttpDelete(url);
					}
					break;
				case POST:
				case PUT:
					HttpEntity entity = TextUtils.isEmpty(query) ? null : new StringEntity(query);
					BasicHeader header = new BasicHeader(HTTP.CONTENT_ENCODING, "application/x-www-form-urlencoded");
					if (method.equals(HttpMethod.PUT)) {
						HttpPut putr = new HttpPut(url);
						if (entity != null) {
							putr.setHeader(header);
							putr.setEntity(entity);
							req = putr;
						}
					} else if (method.equals(HttpMethod.POST)) {
						HttpPost postr = new HttpPost(url);
						if (entity != null) {
							postr.setHeader(header);
							postr.setEntity(entity);
							req = postr;
						}
					}
				}
				HttpResponse httpResponse = HttpManager.execute(req, Utils.DEV_ENV
						, version);
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					logger.warn("HTTP request failed with status code: {}", statusCode);
					logger.warn(httpResponse.getStatusLine().getReasonPhrase());
					throw new IOException("HTTP request failed with status code : "
							+ statusCode);
				}
				bis = new BufferedInputStream(httpResponse.getEntity().getContent());
			} else {

				if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)) 	{
					url = paramz.size() > 0 ? url + "?" + query : url;
				}
				URL uri = new URL(url);
				conn = (HttpURLConnection) uri.openConnection();
				conn.setRequestProperty("User-Agent", userAgent + "/" + version);
				conn.setDoInput(true);
		        conn.setReadTimeout(60 * 1000 /* milliseconds */);
		        conn.setConnectTimeout(60 * 1000 /* milliseconds */);
		        conn.setRequestMethod(method.name());
				if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) 	{
					conn.setDoOutput(true);
					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
					        new OutputStreamWriter(os, "UTF-8"));
					writer.write(query);
					writer.close();
					os.close();
				}
		        int status = conn.getResponseCode();
		        if (status != HttpStatus.SC_OK) {
					logger.warn("HTTP request failed with status code: {}", status);
					logger.warn(conn.getResponseMessage());
					throw new IOException("HTTP request failed with status code : "
							+ status);
		        }
		        bis = new BufferedInputStream(conn.getInputStream());
			}
			int read = 0;
			if (bis != null) {
				byte buffer[] = new byte[buf_size];
				while ((read = bis.read(buffer, 0, buf_size)) != -1) {
					//builder.append(new String(buffer, "utf-8"));
					bab.append(buffer, 0, read);
				}
				builder.append(new String(bab.toByteArray(), "UTF-8"));
			}
			if (conn != null) conn.disconnect();
		} catch (IOException e1) {
			e1.printStackTrace();
			logger.error("HTTP request failed : {}", e1);
			throw e1;
		}
		return builder.toString();
	}

	/**
	 * Uses TelephonyManager and WifiManager to check for network connectivity.
	 * Also incorporates CONNECTING state for retry scenarios.
	 * @param context
	 * @return ConnectionStatus
	 */
	public ConnectionStatus isConnectedOLD(Context context) {
		boolean data = false, wifi = false;
		boolean data_connecting = false, wifi_connecting = false;
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		int ds = tm.getDataState();
		int ws = wm.getWifiState();
		switch (ds) {
		case TelephonyManager.DATA_CONNECTED:
			data = true;
			break;
		case TelephonyManager.DATA_CONNECTING:
			data_connecting = true;
		default:
			data = false;
			data_connecting = false;
		}

		switch (ws) {
		case WifiManager.WIFI_STATE_ENABLING:
			wifi_connecting = true;
		case WifiManager.WIFI_STATE_DISABLING:
		case WifiManager.WIFI_STATE_DISABLED:
		case WifiManager.WIFI_STATE_UNKNOWN:
			wifi = false;
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			WifiInfo wi = wm.getConnectionInfo();
			if (wi != null)
				wifi = true;
			break;
		}

		if (wifi && data) return ConnectionStatus.BOTH_CONNECTED;
		else if (wifi && data_connecting) return ConnectionStatus.WIFI_CONNECTED;
		else if (data && wifi_connecting) return ConnectionStatus.DATA_CONNECTED;
		else if (wifi_connecting || data_connecting) return ConnectionStatus.CONNECTING;
		return ConnectionStatus.NO_CONNECTION;
	}
}
