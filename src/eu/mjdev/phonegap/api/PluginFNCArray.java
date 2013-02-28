package eu.mjdev.phonegap.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PluginFNCArray extends HashMap<String, Method> {
	private static final long serialVersionUID = 1L;
	private static String pluginName;
	private static String pluginMountPoint;

	public PluginFNCArray(Class<?> plugin) {
		try {
			pluginName = plugin.getSimpleName();
			pluginMountPoint = "window";
			if (plugin.isAnnotationPresent(PhoneGapPlugin.class)) {
				PhoneGapPlugin a = plugin.getAnnotation(PhoneGapPlugin.class);
				pluginMountPoint = a.appendTo();
				pluginMountPoint = (pluginMountPoint == null) ? "window"
						: pluginMountPoint;
			}
			Method[] methods = plugin.getMethods();
			for (Method m : methods)
				this.add(m);
		} catch (Exception e) {
			LOG.e(pluginName, e.getMessage());
		}
	}

	private void add(Method m) {
		String name = m.getName();
		if ((!this.containsKey(name))
				&& (m.getReturnType() == PluginResult.class)
				&& (name != "execute")) {
			this.put(m.getName(), m);
			m.setAccessible(true);
		}
	}

	private String getFunctions() {
		List<String> s = new ArrayList<String>(this.size());
		for (String i : this.keySet()) {
			Method m = this.get(i);
			if ((m != null) && (m.isAnnotationPresent(PhoneGapPluginFunction.class))) {
				PhoneGapPluginFunction a = (PhoneGapPluginFunction) m.getAnnotation(PhoneGapPluginFunction.class);
				if (a != null) {
					String fscript = a.jscode();
					String fname = m.getName();
					String fncparams = ((a.params() != null) && (a.params().length > 0)) ? combine(
							a.params(), ",") : "";
					if (fscript != null) {
						fscript = fscript.replace("{fncName}", fname);
						fscript = fscript.replace("{pluginName}", pluginName);
						if (a.params().length > 0)
							fscript = fscript.replace("{fncparams}cbsuccess",(fncparams + ",cbsuccess"));
						else
							fscript = fscript.replace("{fncparams}cbsuccess","cbsuccess");
						fscript = fscript.replace("{fncparams}", fncparams);
						s.add(fscript);
					}
				}
			}
		}
		return combine((String[]) s.toArray(new String[s.size()]), ",");
	}

	private static String combine(String[] s, String glue) {
		int k = s.length;
		if (k == 0) return null;
		StringBuilder out = new StringBuilder();
		out.append(s[0]);
		for (int x = 1; x < k; ++x) out.append(glue).append(s[x]);
		return out.toString();
	}
	
	public String getDescription()
	{   
		return
			"Plugin     :  "+ pluginName + "\n" +
			"details    :\n"+
			"class      : " + this.getClass().getCanonicalName() + "\n" + 
			"plugin name: " + pluginName + "\n" +
			"mount point: " + pluginMountPoint + "\n" 
		;
	}

	public String ToJavaScript() {
		LOG.d("Plugin", this.getDescription());
		return ("$.app.addPlugin('" + pluginName + "',{" + this.getFunctions() + "}, " + pluginMountPoint + ");");
	}
}