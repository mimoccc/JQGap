package eu.mjdev.phonegap.api;

import org.json.JSONArray;
import org.json.JSONObject;

public class PluginResult {
	private final int status;
	private final String message;
	private boolean keepCallback = false;
	
	public PluginResult(Status status) {
		this.status = status.ordinal();
		this.message = "'" + PluginResult.StatusMessages[this.status] + "'";
	}
	
	public PluginResult(Status status, String message) {
		this.status = status.ordinal();
		this.message = JSONObject.quote(message);
	}

	public PluginResult(Status status, JSONArray message) {
		this.status = status.ordinal();
		this.message = message.toString();
	}

	public PluginResult(Status status, JSONObject message) {
		this.status = status.ordinal();
		this.message = message.toString();
	}

	public PluginResult(Status status, int i) {
		this.status = status.ordinal();
		this.message = ""+i;
	}

	public PluginResult(Status status, float f) {
		this.status = status.ordinal();
		this.message = ""+f;
	}

	public PluginResult(Status status, boolean b) {
		this.status = status.ordinal();
		this.message = ""+b;
	}
	
	public void setKeepCallback(boolean b) {
		this.keepCallback = b;
	}
	
	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
	public boolean getKeepCallback() {
		return this.keepCallback;
	}
	
	public String getJSONString() {
		return 
			"{status:" + 
			this.status + 
			",message:" + 
			this.message.replace("\'", "\\\'") + 
			",keepCallback:" + 
			this.keepCallback + 
			"}";
	}
	
	public String toSuccessCallbackString(String callbackId) {
		return "$.app.callbackSuccess('"+callbackId+"',"+this.getJSONString()+");";
	}
	
	public String toErrorCallbackString(String callbackId) {
		return "$.app.callbackError('"+callbackId+"', " + this.getJSONString()+ ");";
	}
	
	public static String[] StatusMessages = new String[] {
		"No result",
		"OK",
		"Class not found",
		"Illegal access",
		"Instantiation error",
		"Malformed url",
		"IO error",
		"Invalid action",
		"JSON error",
		"Error"
	};
	
	public enum Status {
		NO_RESULT,
		OK,
		CLASS_NOT_FOUND_EXCEPTION,
		ILLEGAL_ACCESS_EXCEPTION,
		INSTANTIATION_EXCEPTION,
		MALFORMED_URL_EXCEPTION,
		IO_EXCEPTION,
		INVALID_ACTION,
		JSON_EXCEPTION,
		ERROR
	}
}
