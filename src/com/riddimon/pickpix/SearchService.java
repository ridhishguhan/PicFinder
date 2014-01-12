package com.riddimon.pickpix;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.riddimon.pickpix.api.HttpUtils;
import com.riddimon.pickpix.api.ImageResult;
import com.riddimon.pickpix.api.ImageSearchRequest;
import com.riddimon.pickpix.api.ImageSearchResult;
import com.riddimon.pickpix.api.ServiceRequest;
import com.riddimon.pickpix.util.JsonUtil;
import com.riddimon.pickpix.util.StatusCode;
import com.riddimon.pickpix.util.TaskExecutor;

public class SearchService extends Service {
	public static final String OP = "operation";
	public static final String ACTION = "SEARCH_COMPLETED";

	public static final int OP_SEARCH = 1;
	public static final int OP_DOWNLOAD_IMAGE = 2;

	public static final String EX_SEARCH_REQ = "query";
	public static final String EX_STATUS = "status";

	Set<ServiceRequest> requests = new HashSet<ServiceRequest>();

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int op = intent == null ? -1 : intent.getIntExtra(OP, -1);
		Runnable run = null;
		switch(op) {
		case OP_SEARCH:
			ImageSearchRequest req = intent.getParcelableExtra(EX_SEARCH_REQ);
			run = new SearchRunner(req);
			break;
		case OP_DOWNLOAD_IMAGE:

			break;
		default:
			return super.onStartCommand(intent, flags, startId);
		}
		if (run == null) {
			synchronized(requests) {
				if (requests.size() == 0) {
					stopSelf();
					return super.onStartCommand(intent, flags, startId);
				}
			}
		} else {
			TaskExecutor.submit(this, run);
		}
		return START_STICKY;
	}

	private void storeSearchResults(ImageSearchRequest req, ImageSearchResult results) {
		getContentResolver().bulkInsert(ImageResult.URI
				, results.getStorableResults(req.query)
				.toArray(new ContentValues[0]));
	}

	private void signalCompletion(ServiceRequest request, int status) {
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION)
					.putExtra(EX_STATUS, status));
		synchronized(requests) {
			requests.remove(request);
			if (requests.size() == 0) {
				stopSelf();
			}
		}
	}

	private class SearchRunner implements Runnable {
		private ImageSearchRequest request;

		public SearchRunner(ImageSearchRequest re) {
			this.request = re;
			synchronized(requests) {
				requests.add(re);
			}
		}

		@Override
		public void run() {
			int status = StatusCode.CLIENT_DOWNLOAD_ERROR;
			int retry = 0;
			while (retry < 3 && status != StatusCode.OK) {
				try {
					String res = HttpUtils.execute(SearchService.this, request.method
							, request.getServicePath(), request.getParamz());
					if (!TextUtils.isEmpty(res)) {
						ImageSearchResult result = JsonUtil.forJsonString(res, ImageSearchResult.class);
						if (result != null) {
							status = result.getResponseStatus() == HttpStatus.SC_OK ? StatusCode.OK
									: StatusCode.CLIENT_DOWNLOAD_ERROR;
							if (status == StatusCode.OK) {
								storeSearchResults(request, result);
							}
						} else {
							status = StatusCode.CLIENT_DOWNLOAD_ERROR;
						}
					}
				} catch (IOException e) {
					status = StatusCode.CLIENT_CONNECT_FAILED;
					e.printStackTrace();
				}
				if (status != StatusCode.OK) {
					++retry;
					try {
						Thread.sleep(retry * 500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			signalCompletion(request, status);
		}
	}
}