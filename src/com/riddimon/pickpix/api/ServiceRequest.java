package com.riddimon.pickpix.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.text.TextUtils;

import com.riddimon.pickpix.api.HttpUtils.HttpMethod;

public class ServiceRequest implements Cloneable {
	protected static final Logger logger = LoggerFactory.getLogger(ServiceRequest.class.getSimpleName());
	
	public ServiceApi api = null;
	public HttpMethod method = null;

	protected Map<String, String> paramz = new HashMap<String, String>();

	public ServiceRequest(ServiceApi api, HttpMethod method) {
		this.api = api;
		this.method = method;
	}
	public Map<String, String> getParamz() {
		Map<String, String> p = new HashMap<String, String>();
		synchronized(paramz) {
			p.putAll(paramz);
		}
		return paramz;
	}

	public void setParamz(Map<String, String> paramz) {
		synchronized(this.paramz) {
			if (paramz != null) this.paramz = paramz;
			else this.paramz.clear();
		}
	}

	public List<String> pathParamz = new ArrayList<String>();

	public void addParameter(String key, Boolean val) {
		if (val != null) addParameter(key, val.toString().toLowerCase());
	}

	public void addParameter(String key, long val) {
		addParameter(key, String.valueOf(val));
	}

	public void addParameter(String key, Date val) {
		if (val != null) {
			long ts = val.getTime();
			if (ts != 0) addParameter(key, String.valueOf(ts));
		}
	}
	public void addParameter(String key, String val) {
		if (TextUtils.isEmpty(key) || TextUtils.isEmpty(val)) return;
		synchronized(paramz) {
			if (paramz.containsKey(key)) paramz.remove(key);
			paramz.put(key, val);
		}
	}

	public void addParameters() {
		addParameter("ver", ServiceApi.VERSION);
	}

	protected void setupPathParamz() {}

	public String getServicePath() {
		this.pathParamz.clear();
		setupPathParamz();
		StringBuilder s = new StringBuilder();
		s.append(this.api.getServiceUrl(null));
		for (String p : this.pathParamz) {
			s.append("/").append(p);
		}
		return s.toString();
	}

	public ServiceRequest clone() {
		ServiceRequest clone = null;
		try {
			clone = (ServiceRequest) super.clone();
			clone.paramz = this.getParamz();
		} catch(CloneNotSupportedException ex) {
			// do nothing
		}
		return clone;
	}
}