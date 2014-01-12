package com.riddimon.pickpix.util;

import java.lang.reflect.Field;
import java.util.Map;

public class StatusCode {
	
	// generic errors
	public static final int OK 									= 0;

	// Client side generated errors
	public static final int CLIENT_INVALID_INPUT 				= 100001;
	public static final int CLIENT_CONNECT_FAILED 				= 100002;
	public static final int CLIENT_INVALID_REQUEST 				= 100003;
	public static final int CLIENT_EXCEPTION 					= 100004;
	public static final int CLIENT_IO_ERROR 					= 100005;
	public static final int CLIENT_DOWNLOAD_ERROR 				= 100006;

	protected static Map<Integer, String> messageMap = null;
	
	public static String getErrorMessage(int status) {
		return StatusCode.toString(status);
	}
	
	public static String toString(int status) {
		
		for (Field f : StatusCode.class.getFields()) {
			try {
				if (f.getInt(null) == status) {
					return f.getName();
				}
			} catch (Exception e) {
				return "INVALID_ERROR_CODE";
			}
		}
		return "INVALID_ERROR_CODE";
	}
	
}
