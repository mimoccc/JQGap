package eu.mjdev.phonegap.api;

import android.webkit.WebView;

public class PluginEntry {
	public String service = "";
	public String pluginClass = "";
	public IPlugin plugin = null;
	public boolean onload = false;
	public PluginEntry(String service, String pluginClass, boolean onload) {
		this.service = service;
		this.pluginClass = pluginClass;
		this.onload = onload;
	}
	@SuppressWarnings("rawtypes")
	public IPlugin createPlugin(WebView webView, PhoneGapInterface ctx) {
		if (this.plugin != null) return this.plugin;
		try {
			Class c = getClassByName(this.pluginClass);
			if (isPhoneGapPlugin(c)) {
				this.plugin = (IPlugin) c.newInstance();
				this.plugin.setContext(ctx);
				this.plugin.setView(webView);
				return plugin;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.d(this.getClass().getSimpleName(), "Error adding plugin "+ this.pluginClass + "." + e.getMessage());
		}
		return null;
	}
	private Class<?> getClassByName(final String clazz)
			throws ClassNotFoundException {
		Class<?> c = null;
		if (clazz != null)
			c = Class.forName(clazz);
		return c;
	}
	private boolean isPhoneGapPlugin(Class<?> c) {
		if (c != null)
			return eu.mjdev.phonegap.api.Plugin.class.isAssignableFrom(c) || eu.mjdev.phonegap.api.IPlugin.class.isAssignableFrom(c);
		return false;
	}
	String getPluginScript() {
		String s = "";
		try {
			Class<?> c = getClassByName(this.pluginClass);
			if (isPhoneGapPlugin(c)) {
				IPlugin p = (IPlugin) c.newInstance();
				PluginFNCArray a = p.getActions();
				if (a != null) s = a.ToJavaScript();
				p = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.d(this.getClass().getSimpleName(), "Error injecting plugin script " + this.pluginClass + "." + e.getMessage());
		}
		return s;
	}
}