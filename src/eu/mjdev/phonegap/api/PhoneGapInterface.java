package eu.mjdev.phonegap.api;

import java.util.HashMap;

import eu.mjdev.phonegap.HttpCacheDBHelper;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;

public interface PhoneGapInterface {
    abstract public void addService(String serviceType, String className, PhoneGapInterface ctx);
    
    abstract public void sendJavascript(String statement);

    abstract public void startActivityForResult(IPlugin command, Intent intent, int requestCode);
    
    abstract public void startActivity(Intent intent);
    
    abstract public void setActivityResultCallback(IPlugin plugin);

    abstract public void loadUrl(String url);
    
    abstract public void postMessage(String id, Object data);
    
    public abstract Resources getResources();

    public abstract String getPackageName();

    public abstract Object getSystemService(String service);

    public abstract Context getContext();
    
    public abstract Context getBaseContext();

    public abstract Intent registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter);

    public abstract ContentResolver getContentResolver();

    public abstract void unregisterReceiver(BroadcastReceiver receiver);

    public abstract Cursor managedQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);
    
    public abstract void runOnUiThread(Runnable runnable);

    public abstract AssetManager getAssets();

    public abstract void clearCache();

    public abstract void clearHistory();

    public abstract boolean backHistory();

    public abstract void bindBackButton(boolean override);

    public abstract boolean isBackButtonBound();

    public abstract void cancelLoadUrl();

    public abstract void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params);

    public abstract Context getApplicationContext();

    public abstract boolean isUrlWhiteListed(String source);

	public void addWhiteListEntry(String origin, boolean subdomains);

	public HttpCacheDBHelper getHTTPCache();
}