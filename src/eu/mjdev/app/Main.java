package eu.mjdev.app;

//import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
//import eu.mjdev.gpslogger.GPSSettings;
import eu.mjdev.phonegap.DroidGap;

public class Main extends DroidGap {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setIntegerProperty("backgroundColor", Color.BLACK);
		super.setBooleanProperty("keepRunning", true);
		//GPSSettings.setGpsServiceUrl(this.getString(R.string.gps_service_url));
		super.loadUrl("file:///android_asset/www/index.html");
	}
}