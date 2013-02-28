package eu.mjdev.phonegap;

import eu.mjdev.phonegap.plugins.GeoBroker;
import android.location.LocationManager;

/**
 * This class handles requests for GPS location services.
 *
 */
public class GPSListener extends PhoneGapLocationListener {
	public GPSListener(LocationManager locationManager, GeoBroker m) {
		super(locationManager, m, "[PhoneGap GPSListener]");
	}

	/**
	 * Start requesting location updates.
	 * 
	 * @param interval
	 */
	@Override
	protected void start(int interval) {
		if (!this.running) {
			if (this.locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
				this.running = true;
				this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, this);
			} else {
				this.fail(PhoneGapLocationListener.POSITION_UNAVAILABLE, "GPS provider is not available.");
			}
		}
	}
}
