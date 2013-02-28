package eu.mjdev.phonegap;

import eu.mjdev.phonegap.plugins.GeoBroker;
import android.location.LocationManager;

public class NetworkListener extends PhoneGapLocationListener {
	public NetworkListener(LocationManager locationManager, GeoBroker m) {
		super(locationManager, m, "[PhoneGap NetworkListener]");
	}
}
