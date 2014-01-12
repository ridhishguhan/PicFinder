package com.riddimon.pickpix.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {
	private static Logger logger = LoggerFactory.getLogger(JsonUtil.class.getSimpleName()); 
	
	protected static ObjectMapper mapper = null;
	private static Object locker = new Object();
	
	protected static void init() {
		if (mapper == null) {
			synchronized(locker) {
				if (mapper == null) {
					mapper = new ObjectMapper();
					mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
							, false);
					mapper.configure(DeserializationFeature
							.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
					mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
					mapper.setSerializationInclusion(Include.NON_NULL);
					mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
				}
			}
		}
	}
	
	public static boolean encode(OutputStream out, Object obj) {
		if (out == null || obj == null) {
			return false;
		}
		
		init();
		try {
			mapper.writeValue(out, obj);
		} catch (Exception e) {
			logger.error("Unable to encode object: {}", e);
			return false;
		}
		return true;
	}
	
	public static <T> T decode(InputStream in, Class<T> clazz) {
		if (in == null || clazz == null) { return null; }
		init();
		try {
			if (in.available() <= 0) {
				return null;
			}
			return mapper.readValue(in, clazz);
		} catch (Exception e) {
			logger.error("Unable to encode data object: ", e);
			return null;
		}
	}
	
	public static <T> T decode(InputStream in, TypeReference<T> reference) {
		if (in == null || reference == null) { return null; }
		init();
		try {
			if (in.available() <= 0) {
				return null;
			}
			return mapper.readValue(in, reference);
		} catch (Exception e) {
			logger.error("Unable to encode data object: ", e);
			return null;
		}
	}
	
	public static <T> byte[] toJson(T obj) {
		if (obj == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (encode(out, obj)) {
			return out.toByteArray();
		} else {
			return null;
		}
	}
	
	public static <T> String toJsonString(T obj) {
		byte[] json = toJson(obj);
		if (json == null) { return null; }
		try {
			return new String(json, "utf-8");
		} catch (Exception e) {
			logger.error("Cannot convert from byte array to JSON string: ", e);
			return null;
		}
	}
	
	/// put an object as a string into a map.
	public static void putString(Map<String, Object> map, String key, Object obj) {
		if (map != null && key != null && ! TextUtils.isEmpty(key) && obj != null) {
			map.put(key, obj.toString());
		}
	}

	/// put an object into a map.
	public static void putEntry(Map<String, Object> map, String key, Object obj) {
		if (map != null && key != null && ! TextUtils.isEmpty(key) && obj != null) {
			map.put(key, obj);
		}
	}
	
	/// put a Date object as a timestamp into a map.
	public static void putDate(Map<String, Object> map, String key, Date d) {
		if (map != null && key != null && ! TextUtils.isEmpty(key) && d != null) {
			map.put(key, d.getTime());
		}
	}
	
	public static String getString(Map<String, Object> map, String key) {
		if (map != null && key != null && ! TextUtils.isEmpty(key)) {
			Object obj = map.get(key);
			if (obj != null) {
				return obj.toString();
			}
		}
		return null;
	}
	
	public static Integer getInteger(Map<String, Object> map, String key) {
		if (map != null && key != null && ! TextUtils.isEmpty(key)) {
			Object obj = map.get(key);
			if (obj != null) {
				return new Integer(obj.toString());
			}
		}
		return null;
	}
	
	public static BigInteger getBigInteger(Map<String, Object> map, String key) {
		if (map != null && key != null && ! TextUtils.isEmpty(key)) {
			Object obj = map.get(key);
			if (obj != null) {
				return new BigInteger(obj.toString());
			}
		}
		return null;
	}
	
	public static Date getDate(Map<String, Object> map, String key) {
		if (map != null && key != null && ! TextUtils.isEmpty(key)) {
			Object obj = map.get(key);
			if (obj != null) {
				return new Date(Long.parseLong(obj.toString()));
			}
		}
		return null;
	}
	
	public static Map<String, Object> forJsonString(byte[] s) {
		return forJsonString(s, new TypeReference<Map<String, Object>>(){});
	}
	
	public static Map<String, Object> forJsonString(String s) {
		return forJsonString(s, new TypeReference<Map<String, Object>>(){});
	}
	
	public static <T> T forJsonString(String s, Class<T> clazz) {
		if (s == null || s.length() == 0) {
			return null;
		}
		init(); 
		
		try {
			return mapper.readValue(s, clazz);
		} catch (Exception e) {
			logger.error("Cannot convert from string to object of class {}: {}", clazz.getName(), e);
			logger.error("Offending string {}: ", s);
			return null;
		}
	}
	
	public static <T> T forJsonString(byte[] s, Class<T> clazz) {
		if (s == null || s.length == 0) {
			return null;
		}
		init(); 
		
		try {
			return mapper.readValue(s, clazz);
		} catch (Exception e) {
			logger.error("Cannot convert from string to object of class {}: ", clazz.getName(), e);
			try {
				logger.error("Offending string {}: ", new String(s, "utf8"));
			} catch (Exception e1) {
				logger.error("Unable to decode offending string as utf-8 string. ", e1);
			}
			return null;
		}
	}
	
	public static <T> T forJsonString(String s, TypeReference<T> clazz) {
		if (s == null || s.length() == 0) {
			return null;
		}
		init(); 
		
		try {
			return mapper.readValue(s, clazz);
		} catch (Exception e) {
			logger.error("Cannot convert from string to object of class {}: ", clazz.getClass(), e);
			return null;
		}
	}
	
	public static <T> T forJsonString(byte[] s, TypeReference<T> clazz) {
		if (s == null || s.length == 0) {
			return null;
		}
		init(); 
		
		try {
			return mapper.readValue(s, clazz);
		} catch (Exception e) {
			logger.error("Cannot convert from string to object of class {}: ", clazz.getClass(), e);
			return null;
		}
	}
}
