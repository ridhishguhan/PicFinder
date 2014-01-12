package com.riddimon.pickpix;

import java.io.LineNumberReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class TraceService extends Service {
	public static final String OP = "operation";
	public static final String ACTION = "TRACE_NODE_FINISH";

	public static final int OP_START_TRACE = 1;
	public static final int OP_STOP_TRACE = 2;
	public static final String EX_HOST = "trace_host";
	public static final String EX_STATUS = "status";

	Handler mHandler;
	HandlerThread mThread;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int op = intent == null ? -1 : intent.getIntExtra(OP, -1);
		switch(op) {
		case OP_START_TRACE:
			if (mThread == null || !mThread.isAlive() || mThread.isInterrupted()) {
				mThread = new HandlerThread("trace_thread");
				mThread.start();
				mHandler = new Handler(mThread.getLooper());
			}
			return START_STICKY;
		case OP_STOP_TRACE:
			if (mThread != null && mThread.isAlive() && !mThread.isInterrupted()) {
				mThread.interrupt();
			}
			stopSelf();
		default:
			return super.onStartCommand(intent, flags, startId);
		}
	}

	private void storeThreadNode(TraceNode node) {
		getContentResolver().insert(TraceNode.URI, node.toContentValues());
	}

	private void signalCompletion(boolean success) {
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION)
					.putExtra(EX_STATUS, success));
	}

	private class TraceRunner implements Runnable {
		private boolean udp;
		private int udpPort;
		private String host;
		private long maxDelay;
		private int maxHops;
		private int errors;

		public TraceRunner(String host, long maxDelay, int maxHops, int errors) {
			this.udp = false;
			this.host = host;
			this.maxDelay = maxDelay;
			this.maxHops = maxHops;
			this.errors = errors;
		}

		public void setUdp(boolean udp, int remotePort) {
			this.udp = udp;
			if (udp) {
				this.udpPort = remotePort;
			}
		}

		@Override
		public void run() {
			WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
			ConnectivityManager cm = (ConnectivityManager)
					getSystemService(CONNECTIVITY_SERVICE);
			MulticastLock ml = null;
			boolean success = false;
			
			PipedInputStream pin = null;
			PipedOutputStream pout = null;
			LineNumberReader reader = null;
			Process process = null;
			signalCompletion(success);
		}
	}
}