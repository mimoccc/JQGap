package eu.mjdev.phonegap.api;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.webkit.WebView;

public abstract class Plugin implements IPlugin {
	private static PluginFNCArray actions = null;
	public String id;
	public WebView webView;
	public PhoneGapInterface ctx;
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginFNCArray actions = getActions();
		Method pf = ((actions != null) && (actions.size() > 0) && (actions.containsKey(action))) ? actions.get(action) : null;
		if (pf != null) {
			try {
				return (PluginResult) pf.invoke(this, new Object[] { ctx, this, args, callbackId });
			} catch (Exception e) {
				return resultError(e.getMessage(), callbackId);
			}
		}
		return resultError("Invalid function call, plugin function '" + action + "' does not exists.", callbackId);
	}
	public boolean isSynch(String action) { return false; }
	public void setContext(PhoneGapInterface ctx) { this.ctx = ctx; }
	public void setView(WebView webView) { this.webView = webView; }
	public void onPause(boolean multitasking) {}
	public void onResume(boolean multitasking) {}
	public void onNewIntent(Intent intent) {}
	public void onDestroy() {}
	public void onMessage(String id, Object data) {}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {}
	public boolean onOverrideUrlLoading(String url) { return false; }
	public void sendJavascript(String statement) {
		if (this.ctx != null) this.ctx.sendJavascript(statement);
		else LOG.d(this.getClass().getSimpleName(), "statement " + statement + "' can't execute");
	}
	public void success(PluginResult pluginResult, String callbackId) {
		this.ctx.sendJavascript(pluginResult.toSuccessCallbackString(callbackId));
	}
	public void success(JSONObject message, String callbackId) {
		this.ctx.sendJavascript(new PluginResult(PluginResult.Status.OK, message).toSuccessCallbackString(callbackId));
	}
	public void success(String message, String callbackId) {
		this.ctx.sendJavascript(new PluginResult(PluginResult.Status.OK, message).toSuccessCallbackString(callbackId));
	}
	public void error(PluginResult pluginResult, String callbackId) {
		this.ctx.sendJavascript(pluginResult.toErrorCallbackString(callbackId));
	}
	public void error(JSONObject message, String callbackId) {
		this.ctx.sendJavascript(new PluginResult(PluginResult.Status.ERROR, message).toErrorCallbackString(callbackId));
	}
	public void error(String message, String callbackId) {
		this.ctx.sendJavascript(new PluginResult(PluginResult.Status.ERROR, message).toErrorCallbackString(callbackId));
	}
	public PluginResult resultError(String message, String callbackId) {
		return (new PluginResult(PluginResult.Status.ERROR, message));
	}
	public PluginResult resultOK(String callbackId) {
		return (new PluginResult(PluginResult.Status.OK));
	}
	@Override
	public PluginFNCArray getActions() { return actions; }
}