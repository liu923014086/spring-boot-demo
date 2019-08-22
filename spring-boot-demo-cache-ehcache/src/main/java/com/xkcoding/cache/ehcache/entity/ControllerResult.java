package com.xkcoding.cache.ehcache.entity;

public class ControllerResult {

	public final static char SUCCESS = '1';
	public final static char ERROR = '2';
	public final static char REGEST = '3';
	public final static String SUCCESS_STR = "success";
	public final static String ERROR_STR = "error";
	public final static String REGEST_STR = "regest";

	private String result;
	private String msg;
	private Object object;

	public static ControllerResult valueOf(char result) {
		switch (result) {
		case SUCCESS:
			return new ControllerResult(SUCCESS_STR);
		case ERROR:
			return new ControllerResult(ERROR_STR);
		case REGEST:
			return new ControllerResult(REGEST_STR);
		default:
			return null;
		}
	}

	public static ControllerResult valueOf(char result, String msg) {
		ControllerResult r = ControllerResult.valueOf(result);
		if (r == null) {
			return null;
		}
		r.setMsg(msg);
		return r;
	}

	public static ControllerResult valueOf(char result, Object object) {
		ControllerResult r = ControllerResult.valueOf(result);
		if (r == null) {
			return null;
		}
		r.setObject(object);
		return r;
	}

	public static ControllerResult valueOf(char result, String msg,
			Object object) {
		ControllerResult r = ControllerResult.valueOf(result, msg);
		if (r == null) {
			return null;
		}
		r.setObject(object);
		return r;
	}

	ControllerResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
