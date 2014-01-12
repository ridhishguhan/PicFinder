package com.riddimon.pickpix.api;

public class ServiceApi implements Cloneable {
	public static final String VERSION = "0.5";

	public String protocol;
	public String host;
	public String app;
	public int port;

	public ServiceApi(String protocol, String host, int port, String app) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.app = app;
	}

	public void copyFrom(ServiceApi api) {
		if (api == null) return;
		this.protocol = api.protocol;
		this.host = api.host;
		this.app = api.app;
		this.port = api.port;
	}

	public String getServiceUrl(ServiceRequest req) {
		if (this.protocol == null) this.protocol = "http";
		else this.protocol = this.protocol.toLowerCase();
        if (! this.protocol.equals("http") && ! this.protocol.equals("https")) {
            throw new IllegalArgumentException("Unsupported protocol " + this.protocol);
        }

        StringBuilder s = new StringBuilder();
        s.append(this.protocol).append("://").append(this.host);
        if (this.port != 0) {
            if ( (this.protocol.equals("http") && this.port != 80) ||
                 (this.protocol.equals("https") && this.port != 443)) {
                s.append(":").append(this.port);
            }
        }

        s.append("/").append(this.app);
        if (req != null) {
            s.append(req.getServicePath());
        }
        return s.toString();
	}
}