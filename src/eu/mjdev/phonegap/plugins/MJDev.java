package eu.mjdev.phonegap.plugins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
//import android.location.Location;
//import android.os.IBinder;
import android.util.Base64;
//import cz.ingenium.AssetTrack.AssetEvent;
import eu.mjdev.app.R;
//import eu.mjdev.gpslogger.GpsLoggingService;
//import eu.mjdev.gpslogger.GpsLoggingService.GpsLoggingBinder;
//import eu.mjdev.gpslogger.IGpsLoggerServiceClient;
//import eu.mjdev.gpslogger.Session;
import eu.mjdev.json.XML;
import eu.mjdev.phonegap.HttpHandler;
import eu.mjdev.phonegap.api.LOG;
import eu.mjdev.phonegap.api.PhoneGapInterface;
import eu.mjdev.phonegap.api.PhoneGapPlugin;
import eu.mjdev.phonegap.api.PhoneGapPluginFunction;
import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginFNCArray;
import eu.mjdev.phonegap.api.PluginResult;
import eu.mjdev.phonegap.api.PluginResult.Status;

@PhoneGapPlugin(appendTo = "window")
public class MJDev extends Plugin 
//implements IGpsLoggerServiceClient 
{

	//private GpsLoggingService loggingService;

	/* PLUGIN METHODS */

	//private final ServiceConnection gpsServiceConnection = new ServiceConnection() {
		//public void onServiceDisconnected(ComponentName name) {
			//loggingService = null;
		//}

		//public void onServiceConnected(ComponentName name, IBinder binder) {
			//loggingService = ((GpsLoggingService) ((GpsLoggingBinder) binder).getService());
			//GpsLoggingService.SetServiceClient(MJDev.this);
			//if (Session.isStarted()) {
				//Location l = Session.getCurrentLocationInfo();
				//OnLocationUpdate(l);
			//}
		//}
	//};

	@PhoneGapPluginFunction()
	public PluginResult getAppDetails(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try {
			JSONObject r = new JSONObject();
			r.put("name",        ctx.getContext().getString(R.string.app_name));
			r.put("description", ctx.getContext().getString(R.string.app_description));
			r.put("logo",        getResImageAsString(ctx, R.drawable.icon));
			r.put("api_url",     ctx.getContext().getString(R.string.api_url));
			r.put("market_url",  ctx.getContext().getString(R.string.market_url));
			r.put("author_name", ctx.getContext().getString(R.string.author_name));
			r.put("author_email",ctx.getContext().getString(R.string.author_email));
			r.put("author_url",  ctx.getContext().getString(R.string.author_url));
			r.put("author_phone",ctx.getContext().getString(R.string.author_phone));
			r.put("log_email",   ctx.getContext().getString(R.string.api_log_email));
			r.put("log_url",     ctx.getContext().getString(R.string.api_log_url));
			r.put("feedback_url",ctx.getContext().getString(R.string.api_feedback_url));
			r.put("version",     ctx.getContext().getString(R.string.app_version));
			r.put("mode",        ctx.getContext().getString(R.string.app_mode));
			r.put("status",      ctx.getContext().getString(R.string.app_default_status));
			return (new PluginResult(Status.OK, r));
		} catch (JSONException e) {
			return resultError(e.toString(), callbackId);
		}
	}

	@PhoneGapPluginFunction
	public PluginResult getAppIcon(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		return new PluginResult(Status.OK, getResImageAsString(ctx,	R.drawable.icon));
	}

	@PhoneGapPluginFunction(params = { "name" })
	public PluginResult getResImage(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try {
			return new PluginResult(Status.OK, getResImageAsString(ctx, args.getString(0)));
		} catch (JSONException e) {
			return resultError(e.toString(), callbackId);
		}
	}

	@PhoneGapPluginFunction(params = { "url" })
	public PluginResult getHttpXMLAsJSON(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try {
			return new PluginResult(Status.OK, getHttpXMLAsJSON(ctx, args.getString(0)));
		} catch (JSONException e) {
			return resultError(e.toString(), callbackId);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return resultError(e.toString(), callbackId);
		} catch (IOException e) {
			e.printStackTrace();
			return resultError(e.toString(), callbackId);
		}
	}
	
	@PhoneGapPluginFunction(params = { "url" })
	public PluginResult getHttpString(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		try{
			String s = getHttpString(ctx, args.getString(0)); 
			if(s != null) 
				return new PluginResult(Status.OK, s);
			else
				return resultError("Error loading data...<br/>Are You connected?", callbackId);
		} catch (Exception e) {
			return resultError(e.getMessage(), callbackId);
		}
	}

	//@PhoneGapPluginFunction
	//public PluginResult startGPSLog(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		//this.startGPSLogging();
		//return new PluginResult(Status.OK);
	//}

	//@PhoneGapPluginFunction
	//public PluginResult stopGPSLog(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		//this.stopGPSLogging();
		//return new PluginResult(Status.OK);
	//}

	//@PhoneGapPluginFunction(params = { "et", "en", "etxt" })
	//public PluginResult logEvent(PhoneGapInterface ctx, Plugin p, JSONArray args, String callbackId) {
		//int et;
		//double en;
		//String etxt;
		//try { et = Integer.parseInt(args.getString(0));   } catch (Exception e) { et = 0;    }
		//try { en = Double.parseDouble(args.getString(1)); } catch (Exception e) { en = 0;    }
		//try { etxt = args.getString(2);                   } catch (Exception e) { etxt = ""; }
		//this.onMessage("GPSSERVICE", "got event : (" + et + ", " + en + ", \"" 	+ etxt + "\")");
		//try {
			//loggingService.WriteToLog(new AssetEvent(et, en, etxt));
		//} catch (Exception e) { 
			//this.onMessage("GPSSERVICE", "event log error : " + e.getMessage()); 
		//}
		//this.onMessage("GPSSERVICE", "event logged.");
		//return new PluginResult(Status.OK);
	//}

	/* PRIVATE PLUGIN METHODS */

	@Override
	public void onMessage(String id, Object data) {
		LOG.d(this.getClass().getSimpleName(), data.toString());
	}

	@Override
	public void onDestroy() {
		// onMessage(this.getClass().getSimpleName(),
		// "Destroying MJDev plugin");
		// if (Session.isStarted()) {
		// onMessage(this.getClass().getSimpleName(), "Stopping GPS log.");
		// this.stopGPSLogging();
		// }
	}

	private static String getResImageAsString(PhoneGapInterface ctx, int imgid) {
		try {
			Bitmap bitmap = BitmapFactory.decodeResource(ctx.getContext()
					.getResources(), imgid);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 100, bos);
			byte[] idata = new byte[bos.size()];
			idata = bos.toByteArray();
			String encodedImage = Base64.encodeToString(idata, 0, idata.length, 0);
			bos.close();
			bos = null;
			idata = null;
			return "data:image/png;base64," + encodedImage;
		} catch (IOException e) {
			return "";
		}
	}

	private static String getResImageAsString(PhoneGapInterface ctx,
			String imagename) {
		try {
			int resid = ctx
					.getContext()
					.getResources()
					.getIdentifier(imagename, "drawable",
							ctx.getContext().getPackageName());
			return getResImageAsString(ctx, resid);
		} catch (Exception e) {
			return "";
		}
	}

	private static JSONObject getHttpXMLAsJSON(PhoneGapInterface ctx, String url) throws IllegalStateException, JSONException, IOException {
		return XML.toJSONObject(getHttpString(ctx, url));
	}

	private static String getHttpString(PhoneGapInterface ctx, String url) throws IllegalStateException, IOException {
        HttpHandler http = new HttpHandler(ctx);
		return http.getCached(url);
	}

	//public void startGPSLogging() {
		//StartAndBindGPSService();
		//Context c = this.ctx.getApplicationContext();
		//Intent serviceIntent = new Intent(c, GpsLoggingService.class);
		//serviceIntent.putExtra("immediate", true);
		//c.startService(serviceIntent);
	//}

	//public void stopGPSLogging() {
		//Context c = this.ctx.getApplicationContext();
		//Intent serviceIntent = new Intent(c, GpsLoggingService.class);
		//serviceIntent.putExtra("immediatestop", true);
		//c.startService(serviceIntent);
		//StopAndUnbindGPSService();
	//}

	//private void StartAndBindGPSService() {
		//onMessage("GPSSERVICE", "bindng gps service");
		//Context c = this.ctx.getBaseContext();
		//Intent serviceIntent = new Intent(c, GpsLoggingService.class);
		//c.startService(serviceIntent);
		//c.bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
		//Session.setBoundToService(true);
		//onMessage("GPSSERVICE", "gps service binded");
	//}

	//private void StopAndUnbindGPSService() {
		//onMessage("GPSSERVICE", "unbinding gps service");
		//Context ctx = this.ctx.getBaseContext();
		//if (Session.isBoundToService()) {
			//ctx.unbindService(gpsServiceConnection);
			//Session.setBoundToService(false);
		//}
		//if (!Session.isStarted()) {
			//Intent serviceIntent = new Intent(ctx, GpsLoggingService.class);
			//ctx.stopService(serviceIntent);
		//}
		//onMessage("GPSSERVICE", "gps service unbinded");
	//}

	//@Override
	//public void OnStatusMessage(String message) {
		//onMessage("GPSSERVICE", message);
	//}

	//@Override
	//public void OnFatalMessage(String message) {
		//onMessage("GPSSERVICE", message);
	//}

	//@Override
	//public void OnLocationUpdate(Location loc) {
		//if (loc != null) {
			//onMessage("GPSSERVICE", "accurancy: " + loc.getAccuracy());
			//onMessage("GPSSERVICE", "altitude : " + loc.getAltitude());
			//onMessage("GPSSERVICE", "latitude : " + loc.getLatitude());
			//onMessage("GPSSERVICE", "longitude: " + loc.getLongitude());
			//onMessage("GPSSERVICE", "speed    : " + loc.getSpeed());
			//onMessage("GPSSERVICE", "time     : " + loc.getTime());
		//}
	//}

	//@Override
	//public void OnSatelliteCount(int count) {
	//}

	//@Override
	//public void ClearForm() {
	//}

	//@Override
	//public void OnStopLogging() {
		//onMessage("GPSSERVICE", "stopping gps log");
	//}

	/* Plugin defaults. Change plugin class only !!! */

	static PluginFNCArray actions = null;

	static { actions = new PluginFNCArray(MJDev.class); }

	@Override
	public PluginFNCArray getActions() { return actions; }
	
	//@Override
	public PhoneGapInterface getContext() { return this.ctx; }
	
	//@Override
	public Plugin getPlugin() { return this; }
}