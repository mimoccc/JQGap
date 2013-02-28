package eu.mjdev.phonegap.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginResult;
import eu.mjdev.phonegap.api.PluginResult.Status;
import eu.mjdev.app.R;

public class StatusBarNotification extends Plugin {
	public static final String ACTION_NOTIFY = "notify";
	public static final String ACTION_CLEAR  = "clear";

	public StatusBarNotification () {}
	
	@Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) ctx.getSystemService(ns);
		if (ACTION_NOTIFY.equals(action)) {
			try {
				String title = args.getString(0);
				String body  = args.getString(1);
				Log.d("NotificationPlugin", "Notification: " + title + ", " + body);
				showNotification(title, body);
			} catch (JSONException jsonEx) {
				Log.d("NotificationPlugin", "Got JSON Exception " + jsonEx.getMessage());
				status = Status.JSON_EXCEPTION;
				result = "Got JSON Exception " + jsonEx.getMessage();
			}
		} 
		else if (ACTION_CLEAR.equals(action)) clearNotification();
		else status = PluginResult.Status.INVALID_ACTION;
        return new PluginResult(status, result);
    }

	public void showNotification(CharSequence contentTitle, CharSequence contentText) {
		Context context = this.ctx.getApplicationContext();
        Intent notificationIntent = new Intent(this.ctx.getPackageName());
		android.app.Notification notification = new android.app.Notification(R.drawable.icon, contentTitle, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this.ctx.getContext(), 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        mNotificationManager.notify(1, notification);
	}
	
	public void clearNotification() { mNotificationManager.cancelAll(); }
	
	private NotificationManager mNotificationManager;
}