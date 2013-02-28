package eu.mjdev.phonegap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import eu.mjdev.phonegap.plugins.GeoBroker;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class PhoneGapLocationListener implements LocationListener {
    public static int PERMISSION_DENIED = 1;
    public static int POSITION_UNAVAILABLE = 2;
    public static int TIMEOUT = 3;

    protected LocationManager locationManager;
    private GeoBroker owner;
    protected boolean running = false;
    
    public HashMap<String, String> watches = new HashMap<String, String>();
    private List<String> callbacks = new ArrayList<String>();

    private String TAG = "[PhoneGap Location Listener]";
	
    public PhoneGapLocationListener(LocationManager manager, GeoBroker broker, String tag) {
    	this.locationManager = manager;
    	this.owner = broker;
    	this.TAG = tag;
    }
    
    protected void fail(int code, String message) {
        for (String callbackId: this.callbacks)
        {
        	this.owner.fail(code, message, callbackId);
        }
        this.callbacks.clear();
        
        Iterator<?> it = this.watches.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
            this.owner.fail(code, message, (String)pairs.getValue());
        }
    }
    
    private void win(Location loc) {
    	for (String callbackId: this.callbacks)
        {
        	this.owner.win(loc, callbackId);
        }
        this.callbacks.clear();
        
        Iterator<?> it = this.watches.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
            this.owner.win(loc, (String)pairs.getValue());
        }
    }
    
    /**
     * Location Listener Methods 
     */
    
	/**
	 * Called when the provider is disabled by the user.
	 * 
	 * @param provider
	 */
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Location provider '" + provider + "' disabled.");
		this.fail(POSITION_UNAVAILABLE, "GPS provider disabled.");
	}

	/**
	 * Called when the provider is enabled by the user.
	 * 
	 * @param provider
	 */
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Location provider "+ provider + " has been enabled");
	}

	/**
	 * Called when the provider status changes. This method is called when a 
	 * provider is unable to fetch a location or if the provider has recently 
	 * become available after a period of unavailability.
	 * 
	 * @param provider
	 * @param status
	 * @param extras
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "The status of the provider " + provider + " has changed");
		if (status == 0) {
			Log.d(TAG, provider + " is OUT OF SERVICE");
			this.fail(PhoneGapLocationListener.POSITION_UNAVAILABLE, "Provider " + provider + " is out of service.");
		}
		else if (status == 1) {
			Log.d(TAG, provider + " is TEMPORARILY_UNAVAILABLE");
		}
		else {
			Log.d(TAG, provider + " is AVAILABLE");
		}
	}

	/**
	 * Called when the location has changed.
	 * 
	 * @param location
	 */
	public void onLocationChanged(Location location) {
		Log.d(TAG, "The location has been updated!");
		this.win(location);
	}
	
	// PUBLIC
	
	public int size() {
		return this.watches.size() + this.callbacks.size();
	}
	
	public void addWatch(String timerId, String callbackId, int timerTime) {
		this.watches.put(timerId, callbackId);
		if (this.size() == 1) {
			this.start(timerTime);
		}
	}
	
	public void addCallback(String callbackId, int timerTime) {
		this.callbacks.add(callbackId);
		if (this.size() == 1) {
			this.start(timerTime);
		}
	}
	
	public void clearWatch(String timerId) {
		if (this.watches.containsKey(timerId)) {
			this.watches.remove(timerId);
		}
		if (this.size() == 0) this.stop();
	}
    
    /**
     * Destroy listener.
     */
    public void destroy() {
    	this.stop();
    }
	
	// LOCAL
	
	/**
	 * Start requesting location updates.
	 * 
	 * @param interval
	 */
	protected void start(int interval) {
		if (!this.running) {
			if (this.locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
				this.running = true;
				this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, 10, this);
			} else {
				this.fail(PhoneGapLocationListener.POSITION_UNAVAILABLE, "Network provider is not available.");
			}
		}
	}

	/**
	 * Stop receiving location updates.
	 */
	private void stop() {
		if (this.running) {
			this.locationManager.removeUpdates(this);
			this.running = false;
		}
	}
}
