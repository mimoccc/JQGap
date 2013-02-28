package eu.mjdev.phonegap.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import eu.mjdev.phonegap.api.PhoneGapInterface;
import eu.mjdev.phonegap.api.Plugin;
import eu.mjdev.phonegap.api.PluginResult;
import eu.mjdev.phonegap.api.PluginResult.Status;
import eu.mjdev.app.R;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

public class Notification extends Plugin {
  
  public int confirmResult = -1;
  
  public ProgressDialog progressDialog = null; 
  
  public static final String ACTION_NOTIFY  = "statusbarNotify";
  public static final String ACTION_CLEAR   = "statusbarClear";
  public static final String ACTION_BEEP    = "beep";
  public static final String ACTION_VIBRATE = "vibrate";
  public static final String ACTION_ALERT   = "alert";
  public static final String ACTION_CONFIRM = "confirm";
  public static final String ACTION_ASTART  = "activityStart";
  public static final String ACTION_ASTOP   = "activityStop";
  public static final String ACTION_PSTART  = "progressStart";
  public static final String ACTION_PVALUE  = "progressValue";
  public static final String ACTION_PSTOP   = "progressStop";
  
  public Notification() {}

  public PluginResult execute(String action, JSONArray args, String callbackId) {
    PluginResult.Status status = PluginResult.Status.OK;
    String result = "";   
    
    try {
      if (ACTION_BEEP.equals(action)) {
        this.beep(args.getLong(0));
      }
      else if (ACTION_VIBRATE.equals(action)) {
        this.vibrate(args.getLong(0));
      }
      else if (ACTION_ALERT.equals(action)) {
        this.alert(args.getString(0),args.getString(1),args.getString(2), callbackId);
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
      }
      else if (ACTION_CONFIRM.equals(action)) {
        this.confirm(args.getString(0),args.getString(1),args.getString(2), callbackId);
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
      }
      else if (ACTION_ASTART.equals(action)) {
        this.activityStart(args.getString(0),args.getString(1));
      }
      else if (ACTION_ASTOP.equals(action)) {
        this.activityStop();
      }
      else if (ACTION_PSTART.equals(action)) {
        this.progressStart(args.getString(0),args.getString(1));
      }
      else if (ACTION_PVALUE.equals(action)) {
        this.progressValue(args.getInt(0));
      }
      else if (ACTION_PSTOP.equals(action)) {
        this.progressStop();
      } else if (ACTION_NOTIFY.equals(action)) {
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
    } catch (JSONException e) {
      return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
    }
  }
  
  public boolean isSynch(String action) {
    if      (ACTION_ALERT.equals(action))   return true;
    else if (ACTION_CONFIRM.equals(action)) return true;
    else if (ACTION_ASTART.equals(action))  return true;
    else if (ACTION_ASTOP.equals(action))   return true;
    else if (ACTION_PSTART.equals(action))  return true;
    else if (ACTION_PVALUE.equals(action))  return true;
    else if (ACTION_PSTOP.equals(action))   return true;
    else return false;
  }

  public void beep(long count) {
    Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    Ringtone notification = RingtoneManager.getRingtone(this.ctx.getContext(), ringtone);
    if (notification != null) {
      for (long i = 0; i < count; ++i) {
        notification.play();
        long timeout = 5000;
        while (notification.isPlaying() && (timeout > 0)) {
          timeout = timeout - 100;
          try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
      }
    }
  }
  
  public void vibrate(long time){
    if (time == 0) time = 500;
    Vibrator vibrator = (Vibrator) this.ctx.getSystemService(Context.VIBRATOR_SERVICE);
    vibrator.vibrate(time);
  }
  
  public synchronized void alert(final String message, final String title, final String buttonLabel, final String callbackId) {
    final PhoneGapInterface ctx = this.ctx;
    final Notification notification = this;
    Runnable runnable = new Runnable() {
      public void run() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(ctx.getContext());
        dlg.setMessage(message);
        dlg.setTitle(title);
        dlg.setCancelable(false);
        dlg.setPositiveButton(buttonLabel,
        new AlertDialog.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            notification.success(new PluginResult(PluginResult.Status.OK, 0), callbackId);
          }
        });
        dlg.create();
        dlg.show();
      };
    };
    this.ctx.runOnUiThread(runnable);
  }

  public synchronized void confirm(final String message, final String title, String buttonLabels, final String callbackId) {
    final PhoneGapInterface ctx = this.ctx;
    final Notification notification = this;
    final String[] fButtons = buttonLabels.split(",");
    Runnable runnable = new Runnable() {
      public void run() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(ctx.getContext());
        dlg.setMessage(message);
        dlg.setTitle(title);
        dlg.setCancelable(false);
        if (fButtons.length > 0) {
          dlg.setNegativeButton(fButtons[0],
            new AlertDialog.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                notification.success(new PluginResult(PluginResult.Status.OK, 1), callbackId);
              }
            });
          }
        if (fButtons.length > 1) {
          dlg.setNeutralButton(fButtons[1], 
            new AlertDialog.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                notification.success(new PluginResult(PluginResult.Status.OK, 2), callbackId);
              }
          });
        }
        if (fButtons.length > 2) {
          dlg.setPositiveButton(fButtons[2],
            new AlertDialog.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                notification.success(new PluginResult(PluginResult.Status.OK, 3), callbackId);
              }
            }
          );
        }
        dlg.create();
        dlg.show();
      };
    };
    this.ctx.runOnUiThread(runnable);
  }

  public synchronized void activityStart(final String title, final String message) {}
  
  public synchronized void activityStop() {}

  public synchronized void progressStart(final String title, final String message) {
    final Notification notification = this;
    final PhoneGapInterface ctx = this.ctx;
    Runnable runnable = new Runnable() {
      public void run() {
        notification.progressDialog = new ProgressDialog(ctx.getContext());
        notification.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        notification.progressDialog.setTitle(title);
        notification.progressDialog.setMessage(message);
        notification.progressDialog.setCancelable(true);
        notification.progressDialog.setMax(100);
        notification.progressDialog.setProgress(0);
        notification.progressDialog.setOnCancelListener(
          new DialogInterface.OnCancelListener() { 
            public void onCancel(DialogInterface dialog) {
              notification.progressDialog = null;
            }
          });
        notification.progressDialog.show();
      }
    };
    this.ctx.runOnUiThread(runnable);
  }
  
  public synchronized void progressValue(int value) {
    if (this.progressDialog != null) this.progressDialog.setProgress(value);
  }
  
  public synchronized void progressStop() {
    if (this.progressDialog != null) {
      this.progressDialog.dismiss();
      this.progressDialog = null;
    }
  }

  public void showNotification(CharSequence contentTitle, CharSequence contentText) {
    String ns = Context.NOTIFICATION_SERVICE;
	NotificationManager mNotificationManager = (NotificationManager) this.ctx.getSystemService(ns);
	android.app.Notification notification = new android.app.Notification(R.drawable.icon, contentTitle, System.currentTimeMillis());
	Context context = this.ctx.getApplicationContext();      
    Bundle bundle = new Bundle();
    bundle.putString("action", "view");
    Intent notificationIntent = new Intent(this.ctx.getPackageName());
    notificationIntent.putExtras(bundle);
    notificationIntent.setFlags(
      Intent.FLAG_ACTIVITY_CLEAR_TOP | 
      Intent.FLAG_ACTIVITY_SINGLE_TOP |
      Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
      Intent.FLAG_RECEIVER_REPLACE_PENDING
    );
    PendingIntent contentIntent = PendingIntent.getActivity(this.ctx.getContext(), 0, notificationIntent, 0);
    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);     
    mNotificationManager.notify(1, notification);
  }
	
  public void clearNotification() { 
	  mNotificationManager.cancelAll(); 
  }
	
  private NotificationManager mNotificationManager;
}