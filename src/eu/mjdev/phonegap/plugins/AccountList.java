package eu.mjdev.phonegap.plugins;

import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.accounts.Account;
import android.accounts.AccountManager;

public class AccountList extends Plugin {
	
	private static final String ACTION_GET = "get";
	
	public AccountList () {}
	
	@Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
        if (ACTION_GET.equals(action)) {
			try {
				JSONObject obj = args.getJSONObject(0);
				AccountManager am = AccountManager.get(this.ctx.getContext());
				Account[] accounts;
				if (obj.has("type"))
					accounts = am.getAccountsByType(obj.getString("type"));
				else
					accounts = am.getAccounts();
				JSONArray res = new JSONArray();
				for (int i = 0; i < accounts.length; i++) {
					Account a = accounts[i];
					res.put(a.name);
				}
				return new PluginResult(PluginResult.Status.OK, res);
			} catch (JSONException e) {
				return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
			}
        }
		else {
	        status = PluginResult.Status.INVALID_ACTION;
	    }
        return new PluginResult(status, result);
    }
}