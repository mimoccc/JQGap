JQGap
=====

JQGap

Jquery based phonegap with selfregistration plugins
Many improvements
More usefull plugins

Manual inside

Release date : 25.2.2013

Plugins will be released later, need to refactore

Example plugin:

<pre>
/****
*** Plugin example
****/

package eu.mjdev.phonegap.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import eu.mjdev.app.R;
import eu.mjdev.phonegap.api.PhoneGapInterface;
import eu.mjdev.phonegap.api.PhoneGapPlugin;
import eu.mjdev.phonegap.api.PhoneGapPluginFunction;
import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginFNCArray;
import eu.mjdev.phonegap.api.PluginResult;
import eu.mjdev.phonegap.api.PluginResult.Status;

/* 
mount point of plugin javascript object ! importand, default window
plugin can be called then window.<plugin_name>.<fnc_name>(<fnc_params>); 
*/
@PhoneGapPlugin(appendTo = "window")
public class <b>MJDev</b> extends Plugin
{
  
	/* 
	PLUGIN METHODS 
	*/
  
	@PhoneGapPluginFunction()
	// this means that this is exportable javascript fnc
	public PluginResult getAppDetails(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) 
	{
		try {
			JSONObject r = new JSONObject();
			Context cctx = ctx.getContext();
			r.put("name",        cctx.getString(R.string.app_name));
			r.put("description", cctx.getString(R.string.app_description));
			r.put("api_url",     cctx.getString(R.string.api_url));
			r.put("market_url",  cctx.getString(R.string.market_url));
			r.put("author_name", cctx.getString(R.string.author_name));
			r.put("author_email",cctx.getString(R.string.author_email));
			r.put("author_url",  cctx.getString(R.string.author_url));
			r.put("author_phone",cctx.getString(R.string.author_phone));
			r.put("log_email",   cctx.getString(R.string.api_log_email));
			r.put("log_url",     cctx.getString(R.string.api_log_url));
			r.put("feedback_url",cctx.getString(R.string.api_feedback_url));
			r.put("version",     cctx.getString(R.string.app_version));
			r.put("mode",        cctx.getString(R.string.app_mode));
			r.put("status",      cctx.getString(R.string.app_default_status));
			return (new PluginResult(Status.OK, r));
		} catch (JSONException e) {
			return resultError(e.toString(), callbackId);
		}
	}

	/* Plugin defaults. Do not change, if You dont know what are you doing. */
	static PluginFNCArray actions = null;
	
	static 
	{ 
		actions = new PluginFNCArray(<b>MJDev</b>.class); 
	}
	
	@Override
	public PluginFNCArray getActions() { return actions; }
	
	public PhoneGapInterface getContext() { return this.ctx; }
	
	public Plugin getPlugin() { return this; }
}
/* 
thats all 
*/
</pre>
