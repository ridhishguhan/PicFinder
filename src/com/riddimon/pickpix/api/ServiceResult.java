package com.riddimon.pickpix.api;


public class ServiceResult {
	private String responseDetails;
	private int status;

	public ServiceResult() {
		status = 0;
	}

	public String getResponseDetails() {
		return responseDetails;
	}

	public void setResponseDetails(String responseDetails) {
		this.responseDetails = responseDetails;
	}

	public int getResponseStatus() {
		return status;
	}

	public void setResponseStatus(int status) {
		this.status = status;
	}
}