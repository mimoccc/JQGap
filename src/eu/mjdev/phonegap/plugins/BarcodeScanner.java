package eu.mjdev.phonegap.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginResult;

public class BarcodeScanner extends Plugin {
    
	private static final String ACTION_ENCODE = "encode";
	private static final String ACTION_SCAN   = "encode";
	
	private static final String TEXT_TYPE = "TEXT_TYPE";
    @SuppressWarnings("unused")
	private static final String EMAIL_TYPE = "EMAIL_TYPE";
    @SuppressWarnings("unused")
	private static final String PHONE_TYPE = "PHONE_TYPE";
    @SuppressWarnings("unused")
	private static final String SMS_TYPE = "SMS_TYPE";
    public static final int REQUEST_CODE = 0x0ba7c0de;
    public String callback;

    public BarcodeScanner() {}
   
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
        
        if (ACTION_ENCODE.equals(action)) {
            JSONObject obj = args.optJSONObject(0);
            if (obj != null) {
                String type = obj.optString("type");
                String data = obj.optString("data");
                if (type == null) type = TEXT_TYPE;
                if (data == null) return new PluginResult(PluginResult.Status.ERROR, "User did not specify data to encode");                                            
                encode(type, data);                    
            } else {
                return new PluginResult(PluginResult.Status.ERROR, "User did not specify data to encode");                    
            }
        }
        else if (ACTION_SCAN.equals(action)) scan();
        else {
            status = PluginResult.Status.INVALID_ACTION;
        }
        return new PluginResult(status, result);
    }

    public void scan() {
        Intent intentScan = new Intent("com.phonegap.plugins.barcodescanner.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        this.ctx.startActivityForResult((Plugin) this, intentScan, REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("text", intent.getStringExtra("SCAN_RESULT"));
                    obj.put("format", intent.getStringExtra("SCAN_RESULT_FORMAT"));
                    obj.put("cancelled", false);
                } catch(JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
            } if (resultCode == Activity.RESULT_CANCELED) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("text", "");
                    obj.put("format", "");
                    obj.put("cancelled", true);
                } catch(JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
            } else {
                this.error(new PluginResult(PluginResult.Status.ERROR), this.callback);
            }
        }
    }

    public void encode(String type, String data) {
        Intent intentEncode = new Intent("com.phonegap.plugins.barcodescanner.ENCODE");
        intentEncode.putExtra("ENCODE_TYPE", type);
        intentEncode.putExtra("ENCODE_DATA", data);
        this.ctx.startActivity(intentEncode);
    }
    
}