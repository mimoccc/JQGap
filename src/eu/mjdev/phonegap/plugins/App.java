package eu.mjdev.phonegap.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import eu.mjdev.phonegap.DroidGap;
import eu.mjdev.phonegap.api.LOG;
import eu.mjdev.phonegap.api.PhoneGapInterface;
import eu.mjdev.phonegap.api.PhoneGapPlugin;
import eu.mjdev.phonegap.api.PhoneGapPluginFunction;
import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginFNCArray;
import eu.mjdev.phonegap.api.PluginResult;
import java.util.HashMap;

@PhoneGapPlugin(appendTo = "window")
public class App extends Plugin {

	@PhoneGapPluginFunction()
	public PluginResult clearCache(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			this.clearCache();
			return this.resultOK(callbackId);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
        }
	}
	
	@PhoneGapPluginFunction()
	public PluginResult loadUrl(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			this.loadUrl(args.getString(0), args.optJSONObject(1));
			return this.resultOK(callbackId);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
        }
	}
	
	@PhoneGapPluginFunction()
	public PluginResult cancelLoadUrl(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			this.cancelLoadUrl();
			return this.resultOK(callbackId);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
        }
	}
	
	@PhoneGapPluginFunction()
	public PluginResult clearHistory(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			this.clearHistory();
			return this.resultOK(callbackId);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
        }
	}
	
	@PhoneGapPluginFunction()
	public PluginResult backHistory(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			this.backHistory();
			return this.resultOK(callbackId);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
		}
	}
	
	@PhoneGapPluginFunction()
	public PluginResult isBackbuttonOverridden(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK; 
		try{
			this.overrideBackbutton(args.getBoolean(0));
			boolean b = this.isBackbuttonOverridden();
			return new PluginResult(status, b);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
		}
	}
	
	@PhoneGapPluginFunction()
	public PluginResult exitApp(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			this.exitApp();
			return this.resultOK(callbackId);
		} catch (Exception e) {
			return this.resultError(e.getMessage(), callbackId);
		}
	}

	//--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	public void clearCache() { ((DroidGap)this.ctx).clearCache(); }
	
	public void loadUrl(String url, JSONObject props) throws JSONException {
		int wait = 0;
		boolean openExternal = false;
		boolean clearHistory = false;
		HashMap<String, Object> params = new HashMap<String, Object>();
		if (props != null) {
			JSONArray keys = props.names();
			for (int i=0; i<keys.length(); i++) {
				String key = keys.getString(i); 
				if (key.equals("wait"))                        wait = props.getInt(key);
				else if (key.equalsIgnoreCase("openexternal")) openExternal = props.getBoolean(key);
				else if (key.equalsIgnoreCase("clearhistory")) clearHistory = props.getBoolean(key);
				else {
					Object value = props.get(key);
					if (value == null) {
					} else if (value.getClass().equals(String.class)) {
						params.put(key, (String)value);
					} else if (value.getClass().equals(Boolean.class)) {
						params.put(key, (Boolean)value);
					} else if (value.getClass().equals(Integer.class)) {
						params.put(key, (Integer)value);
					}
				}
			}
		}
		if (wait > 0) {
			try {
				synchronized(this) {this.wait(wait);}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		((DroidGap)this.ctx).showWebPage(url, openExternal, clearHistory, params);
	}

	public void cancelLoadUrl() { ((DroidGap)this.ctx).cancelLoadUrl(); }
	
    public void clearHistory() { ((DroidGap)this.ctx).clearHistory(); }
    
    public void backHistory() { ((DroidGap)this.ctx).backHistory(); }

    public void overrideBackbutton(boolean override) {
    	LOG.i("PhoneGapLog", "WARNING: Back Button Default Behaviour will be overridden.  The backbutton event will be fired!");
    	((DroidGap)this.ctx).bound = override;
    }

    public boolean isBackbuttonOverridden() { return ((DroidGap)this.ctx).bound; }

    public void exitApp() { ((DroidGap)this.ctx).endActivity(); }
    
	/* Plugin defaults. Change plugin class only !!! */
	static PluginFNCArray actions = null;
	static { actions = new PluginFNCArray(App.class); }
	@Override
	public PluginFNCArray getActions() { return actions; }
	//@Override
	public PhoneGapInterface getContext() { return this.ctx; }
	//@Override
	public Plugin getPlugin() { return this; }
}