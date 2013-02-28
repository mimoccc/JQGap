package eu.mjdev.phonegap;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import eu.mjdev.app.Main;
import eu.mjdev.app.R;
import eu.mjdev.phonegap.api.LOG;

public class PhoneGapService extends Service {

	static String TAG = "PhoneGapService";

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	NotificationManager mNM;
	android.app.Notification notification;
	PendingIntent contentIntent;

	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	int mValue = 0;

	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_VALUE = 3;
	static final int MSG_GET_GPS_POSITION = 4;
	static final int MSG_GET_HTTP_CONTENT = 5;
	static final int MSG_RM_NOTIFY = 6;
	static final int MSG_SET_NOTIFY = 7;

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			LOG.i(TAG, "Got message : " + msg.toString());
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_VALUE:
				mValue = msg.arg1;
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(
								Message.obtain(null, MSG_SET_VALUE, mValue, 0));
					} catch (RemoteException e) {
						mClients.remove(i);
					}
				}
				break;
			case MSG_SET_NOTIFY:
				Bundle b = msg.getData();
				String value = ((b == null) ? "" : b.getString("message"));
				showNotification((value == null) ? "" : value);
				break;
			case MSG_RM_NOTIFY:
				hideNotification();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private void hideNotification() {
		mNM.cancel(mValue);
	}

	private void showNotification(String text) {
		CharSequence appname = getText(R.string.app_name);
		if (notification != null)
			hideNotification();
		notification = new android.app.Notification(R.drawable.icon, text,
				System.currentTimeMillis());
		notification.flags = android.app.Notification.FLAG_NO_CLEAR;
		Intent notificationIntent = new Intent(this, Main.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		contentIntent = PendingIntent.getActivity(this.getApplicationContext(),
				0, notificationIntent, 0);
		notification.setLatestEventInfo(this.getApplicationContext(), appname,
				text, contentIntent);
		mNM.notify(mValue, notification);
	}

}
