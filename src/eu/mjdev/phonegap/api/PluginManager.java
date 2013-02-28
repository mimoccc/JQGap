package eu.mjdev.phonegap.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.webkit.WebView;

public class PluginManager {
	private static String TAG = "PhonegapLog - PluginManager";
	private final HashMap<String, PluginEntry> entries = new HashMap<String, PluginEntry>();
	private final PhoneGapInterface ctx;
	private final WebView app;
	private boolean firstRun;
	protected HashMap<String, String> urlMap = new HashMap<String, String>();

	public PluginManager(WebView app, PhoneGapInterface ctx) {
		this.ctx = ctx;
		this.app = app;
		this.firstRun = true;
	}

	@SuppressWarnings("rawtypes")
	private List<String> getPlugins() {
		List<String> plugins = new ArrayList<String>();
		Package pkg = Package.getPackage("eu.mjdev.phonegap.plugins");
		LOG.d(this.getClass().getSimpleName(), "Retrieving plugins");
		Class[] classes = pkg.getClass().getDeclaredClasses();
		LOG.d(this.getClass().getSimpleName(), "plugins: " + classes.length);
		for (Class c : classes) {
			plugins.add(c.getName());
			LOG.d("Plugin", c.getName());
		}
		return plugins;
	}

	public void init() {
		LOG.d(TAG, "init()");
		if (firstRun) {
			this.loadPlugins();
			firstRun = false;
		} else {
			this.onPause(false);
			this.onDestroy();
			this.clearPluginObjects();
		}
		this.startupPlugins();
	}

	public void loadPlugins() {
		List<String> plugins = getPlugins();
		if (plugins.size() > 0) {
			// to do plugins load from reflection
		} else {
			int id = ctx.getResources().getIdentifier("plugins", "xml", ctx.getPackageName());
			if (id == 0) pluginConfigurationMissing();
			XmlResourceParser xml = ctx.getResources().getXml(id);
			int eventType = -1;
			String plugin = "", pluginClass = "";
			boolean onload = false;
			PluginEntry entry = null;
			while (eventType != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String strNode = xml.getName();
					if (strNode.equals("plugin")) {
						plugin = xml.getAttributeValue(null, "name");
						pluginClass = xml.getAttributeValue(null, "value");
						onload = "true".equals(xml.getAttributeValue(null, "onload"));
						entry = new PluginEntry(plugin, pluginClass, onload);
						this.addService(entry);
					} else if (strNode.equals("url-filter")) {
						this.urlMap.put(xml.getAttributeValue(null, "value"), plugin);
					}
				}
				try {
					eventType = xml.next();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.registerPluginsScripts();
	}

	public void clearPluginObjects() {
		for (PluginEntry entry : this.entries.values())	entry.plugin = null;
	}

	public void startupPlugins() {
		for (PluginEntry entry : this.entries.values()) if (entry.onload) entry.createPlugin(this.app, this.ctx);
	}

	public void registerPluginsScripts() {
		for (PluginEntry entry : this.entries.values()) {
			String ss = entry.getPluginScript();
			if((ss!= null)&&(ss.length()>0)) this.sendJavascript(ss);
		}
	}
	
	public void sendJavascript(String statement) { this.ctx.sendJavascript(statement); }

	public String exec(final String service, final String action, final String callbackId, final String jsonArgs, final boolean async) {
		LOG.d(TAG, "exec(" + service + "." + action + ")");
		PluginResult cr = null;
		boolean runAsync = async;
		try {
			final JSONArray args = new JSONArray(jsonArgs);
			
			final IPlugin plugin = this.getPlugin(service);
			
			final PhoneGapInterface ctx = this.ctx;
			
			if (plugin != null) {
				runAsync = async && !plugin.isSynch(action);
				if (runAsync) {
					Thread thread = new Thread(new Runnable() {
						public void run() {
							try {
								PluginResult cr = plugin.execute(action, args, callbackId);
								int status = cr.getStatus();
								if ((status == PluginResult.Status.NO_RESULT.ordinal()) && cr.getKeepCallback()) {
									ctx.sendJavascript(cr.toSuccessCallbackString(callbackId));
								} else if ((status == PluginResult.Status.OK.ordinal())|| (status == PluginResult.Status.NO_RESULT.ordinal())) {
									ctx.sendJavascript(cr.toSuccessCallbackString(callbackId));
								} else
									ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
							} catch (Exception e) {
								PluginResult cr = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
								ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
							}
						}
					});
					thread.start();
					return "";
				} else {
					cr = plugin.execute(action, args, callbackId);
					if ((cr.getStatus() == PluginResult.Status.NO_RESULT
							.ordinal()) && cr.getKeepCallback()) {
						return "";
					}
				}
			}
		} catch (JSONException e) {
			LOG.d(this.getClass().getSimpleName(), "ERROR: " + e.toString());
			cr = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
		if (runAsync) {
			if (cr == null)
				cr = new PluginResult(
						PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION);
			ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
		}
		return (cr != null ? cr.getJSONString()
				: "{ status: 0, message: 'all good' }");
	}

	private IPlugin getPlugin(String service) {
		PluginEntry entry = entries.get(service);
		if (entry == null)
			return null;
		IPlugin plugin = entry.plugin;
		if (plugin == null)
			plugin = entry.createPlugin(this.app, this.ctx);
		return plugin;
	}

	public void addService(String service, String className, PhoneGapInterface ctx) {
		this.addService(new PluginEntry(service, className, false));
	}

	public void addService(PluginEntry entry) {
		this.entries.put(entry.service, entry);
	}

	public void onPause(boolean multitasking) {
		for (PluginEntry entry : this.entries.values())
			if (entry.plugin != null)
				entry.plugin.onPause(multitasking);
	}

	public void onResume(boolean multitasking) {
		for (PluginEntry entry : this.entries.values())
			if (entry.plugin != null)
				entry.plugin.onResume(multitasking);
	}

	public void onDestroy() {
		for (PluginEntry entry : this.entries.values()) if (entry.plugin != null) entry.plugin.onDestroy();
	}

	public void postMessage(String id, Object data) {
		if ((data != null) && (id != null)) {
			for (PluginEntry entry : this.entries.values()) {
				if (entry.plugin != null) {
					LOG.d(this.getClass().getSimpleName(), "sending message to plugin: " + entry.pluginClass + ", message: (" + id + "," + data.toString() + ")");
					entry.plugin.onMessage(id, data);
				}
			}
		}
	}

	public void onNewIntent(Intent intent) {
		for (PluginEntry entry : this.entries.values()) if (entry.plugin != null) entry.plugin.onNewIntent(intent);
	}

	public boolean onOverrideUrlLoading(String url) {
		Iterator<Entry<String, String>> it = this.urlMap.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry<String, String> pairs = it.next();
			if (url.startsWith(pairs.getKey())) return this.getPlugin(pairs.getValue()).onOverrideUrlLoading(url);
		}
		return false;
	}

	private void pluginConfigurationMissing() {
		LOG.d(this.getClass().getSimpleName(),"=====================================================================================");
		LOG.d(this.getClass().getSimpleName(),"ERROR: plugin.xml is missing.  Add res/xml/plugins.xml to your project.");
		LOG.d(this.getClass().getSimpleName(),"=====================================================================================");
	}
}