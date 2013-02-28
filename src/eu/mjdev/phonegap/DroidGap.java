package eu.mjdev.phonegap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParserException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
//import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.Messenger;
//import android.os.RemoteException;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import eu.mjdev.app.R;
import eu.mjdev.phonegap.api.IPlugin;
import eu.mjdev.phonegap.api.LOG;
import eu.mjdev.phonegap.api.PhoneGapInterface;
import eu.mjdev.phonegap.api.PluginManager;

@SuppressLint("SetJavaScriptEnabled")
public class DroidGap extends Activity implements PhoneGapInterface {
	public static String TAG = "JQPhoneGap";
	protected WebView appView;
	protected WebViewClient webViewClient;
	private ArrayList<Pattern> whiteList = new ArrayList<Pattern>();
	private HashMap<String, Boolean> whiteListCache = new HashMap<String, Boolean>();
	protected LinearLayout root;
	public boolean bound = false;
	public CallbackServer callbackServer;
	protected PluginManager pluginManager;
	protected boolean cancelLoadUrl = false;
	private String url = null;
	private Stack<String> urls = new Stack<String>();
	private String initUrl = null;
	private static int ACTIVITY_STARTING = 0;
	private static int ACTIVITY_RUNNING = 1;
	private static int ACTIVITY_EXITING = 2;
	private int activityState = 0; // 0=starting, 1=running (after 1st resume), 2=shutting down
	public String baseUrl = null;
	protected IPlugin activityResultCallback = null;
	protected boolean activityResultKeepRunning;
	int loadUrlTimeout = 0;
	private int backgroundColor = Color.WHITE;
	private Hashtable<String, AuthenticationToken> authenticationTokens = new Hashtable<String, AuthenticationToken>();
	protected int loadUrlTimeoutValue = 20000;
	protected boolean keepRunning = true;
	protected PreferenceSet preferences;
	private HttpCacheDBHelper cacheHelper = null;
	//final Messenger mMessenger = new Messenger(new IncomingHandler());
	Context context;
	//Messenger mService = null;
	//boolean mIsBound;
	public void setAuthenticationToken(AuthenticationToken authenticationToken, String host, String realm) {
		if (host == null)  host = "";
		if (realm == null) realm = "";
		authenticationTokens.put(host.concat(realm), authenticationToken);
	}
	public AuthenticationToken removeAuthenticationToken(String host, String realm) {
		return authenticationTokens.remove(host.concat(realm));
	}
	public AuthenticationToken getAuthenticationToken(String host, String realm) {
		AuthenticationToken token = null;
		token = authenticationTokens.get(host.concat(realm));
		if (token == null) {
			token = authenticationTokens.get(host);
			if (token == null) token = authenticationTokens.get(realm);
			if (token == null) token = authenticationTokens.get("");
		}
		return token;
	}
	public void clearAuthenticationTokens() { authenticationTokens.clear(); }
	public HttpCacheDBHelper getHTTPCache(){ return this.cacheHelper; }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		context = this.getApplicationContext();
		preferences = new PreferenceSet();
		cacheHelper = new HttpCacheDBHelper(context);
		this.loadConfiguration();
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		if (preferences.prefMatches("fullscreen", "true")) 
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else 
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		root = new LinearLayoutSoftKeyboardDetect(this, width, height);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setBackgroundColor(this.backgroundColor);
		root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT, 0.0F));
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String url = bundle.getString("url");
			if (url != null) this.initUrl = url;
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//this.doBindService();
	}
	public void init() { this.init(new WebView(DroidGap.this), new PhoneGapWebViewClient(this), new PhoneGapChromeClient(DroidGap.this)); }
	public void init(WebView webView, WebViewClient webViewClient, WebChromeClient webChromeClient) {
		LOG.d(TAG, "DroidGap.init()");
		this.appView = webView;
		this.appView.setId(100);
		this.appView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT, 1.0F));
		this.appView.setWebChromeClient(webChromeClient);
		this.setWebViewClient(this.appView, webViewClient);
		this.appView.setInitialScale(0);
		this.appView.setVerticalScrollBarEnabled(false);
		this.appView.requestFocusFromTouch();
		WebSettings settings = this.appView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		//settings.setNavDump(true);
		settings.setDatabaseEnabled(true);
		String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		settings.setDatabasePath(databasePath);
		settings.setDomStorageEnabled(true);
		settings.setGeolocationEnabled(true);
		this.appView.setVisibility(View.INVISIBLE);
		root.addView(this.appView);
		setContentView(root);
		this.cancelLoadUrl = false;
		this.pluginManager = new PluginManager(this.appView, this);
	}
	protected void setWebViewClient(WebView appView, WebViewClient client) {
		this.webViewClient = client;
		appView.setWebViewClient(client);
	}
	private void handleActivityParameters() {
		this.backgroundColor = this.getIntegerProperty("backgroundColor", Color.BLACK);
		this.root.setBackgroundColor(this.backgroundColor);
		int timeout = this.getIntegerProperty("loadUrlTimeoutValue", 0);
		if (timeout > 0) this.loadUrlTimeoutValue = timeout;
		this.keepRunning = this.getBooleanProperty("keepRunning", true);
	}
	public void loadUrl(String url) {
		if (this.initUrl == null || (this.urls.size() > 0)) this.loadUrlIntoView(url);
		else this.loadUrlIntoView(this.initUrl);
	}
	private void loadUrlIntoView(final String url) {
		if (!url.startsWith("javascript:")) LOG.d(TAG, "DroidGap.loadUrl(%s)", url);
		this.url = url;
		if (this.baseUrl == null) {
			int i = url.lastIndexOf('/');
			if (i > 0) this.baseUrl = url.substring(0, i + 1);
			else       this.baseUrl = this.url + "/";
		}
		if (!url.startsWith("javascript:")) LOG.d(TAG, "DroidGap: url=%s baseUrl=%s", url, baseUrl);
		final DroidGap me = this;
		this.runOnUiThread(new Runnable() {
			public void run() {
				if (me.appView == null) me.init();
				me.handleActivityParameters();
				me.urls.push(url);
				me.appView.clearHistory();
				if (me.callbackServer == null) {
					me.callbackServer = new CallbackServer();
					me.callbackServer.init(url);
				} else me.callbackServer.reinit(url);
				me.pluginManager.init();
				final int currentLoadUrlTimeout = me.loadUrlTimeout;
				Runnable runnable = new Runnable() {
					public void run() {
						try { synchronized (this) { wait(me.loadUrlTimeoutValue); } } catch (InterruptedException e) { e.printStackTrace(); }
						if (me.loadUrlTimeout == currentLoadUrlTimeout) 
							me.appView.stopLoading();
							//me.webViewClient.onReceivedError(me.appView,-6,"The connection to the server was unsuccessful.",url);
					}
				};
				Thread thread = new Thread(runnable);
				thread.start();
				me.appView.loadUrl(url);
			}
		});
	}
	public void loadUrl(final String url, int time) {
		if (this.initUrl == null || (this.urls.size() > 0)) this.loadUrlIntoView(url, time);
		else                                           		this.loadUrlIntoView(this.initUrl);
	}
	private void loadUrlIntoView(final String url, final int time) {
		this.cancelLoadUrl = false;
		if (this.urls.size() > 0) this.loadUrlIntoView(url);
		this.handleActivityParameters();
		this.loadUrlIntoView(url);
	}
	public void cancelLoadUrl() { this.cancelLoadUrl = true; }
	public void clearCache() {
		if (this.appView == null) this.init();
		this.appView.clearCache(true);
	}
	public void clearHistory() {
		this.urls.clear();
		this.appView.clearHistory();
		if (this.url != null) this.urls.push(this.url);
	}
	public boolean backHistory() {
		if (this.appView.canGoBack()) {
			this.appView.goBack();
			return true;
		}
		if (this.urls.size() > 1) {
			this.urls.pop();
			String url = this.urls.pop();
			this.loadUrl(url);
			return true;
		}
		return false;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) { super.onConfigurationChanged(newConfig); }
	public boolean getBooleanProperty(String name, boolean defaultValue) { 
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) return defaultValue;
		Boolean p = (Boolean) bundle.get(name);
		if (p == null) return defaultValue;
		return p.booleanValue();
	}
	public int getIntegerProperty(String name, int defaultValue) {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) return defaultValue;
		Integer p = (Integer) bundle.get(name);
		if (p == null) return defaultValue;
		return p.intValue();
	}
	public String getStringProperty(String name, String defaultValue) {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) return defaultValue;
		String p = bundle.getString(name);
		if (p == null) return defaultValue;
		return p;
	}
	public double getDoubleProperty(String name, double defaultValue) {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) return defaultValue;
		Double p = (Double) bundle.get(name);
		if (p == null) return defaultValue;
		return p.doubleValue();
	}
	public void setBooleanProperty(String name, boolean value) { this.getIntent().putExtra(name, value); }
	public void setIntegerProperty(String name, int value) { this.getIntent().putExtra(name, value); }
	public void setStringProperty(String name, String value) { this.getIntent().putExtra(name, value); }
	public void setDoubleProperty(String name, double value) { this.getIntent().putExtra(name, value); }
	@Override
	protected void onPause() {
		super.onPause();
		if (this.activityState == ACTIVITY_EXITING) return;
		this.sendJavascript("if($||false) $(window).triggerHandler('app_paused');");
		if (this.pluginManager != null) this.pluginManager.onPause(this.keepRunning);
		if (!this.keepRunning) this.appView.pauseTimers(); 
		//if (mService != null) {
			//try {
				//Message msg = Message.obtain(null,PhoneGapService.MSG_SET_NOTIFY);
				//msg.replyTo = mMessenger;
				//Bundle b = new Bundle();
				//b.putString("message", "Click here to restore application");
				//mService.send(msg);
			//} catch (RemoteException e) {
			//}
		//}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (this.pluginManager != null) this.pluginManager.onNewIntent(intent);
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (this.activityState == ACTIVITY_STARTING) {
			this.activityState = ACTIVITY_RUNNING;
			return;
		}
		if (this.appView == null) return;
		this.sendJavascript("if($||false) $(window).triggerHandler('app_resumed');");
		if (this.pluginManager != null) this.pluginManager.onResume(this.keepRunning|| this.activityResultKeepRunning);
		if (!this.keepRunning || this.activityResultKeepRunning) {
			if (this.activityResultKeepRunning) {
				this.keepRunning = this.activityResultKeepRunning;
				this.activityResultKeepRunning = false;
			}
			this.appView.resumeTimers();
			//if (mService != null) {
				//try {
					//Message msg = Message.obtain(null, PhoneGapService.MSG_RM_NOTIFY);
					//msg.replyTo = mMessenger;
					//mService.send(msg);
				//} catch (RemoteException e) {
				//}
			//}
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(this.cacheHelper != null) this.cacheHelper.close();
		if (this.appView != null) {
			this.sendJavascript("if($||false) $(window).triggerHandler('app_kill');");
			if (this.pluginManager != null) this.pluginManager.onDestroy();
		} else this.endActivity();
	}
	public void postMessage(String id, Object data) { if (this.pluginManager != null) this.pluginManager.postMessage(id, data); }
	public void addService(String serviceType, String className, PhoneGapInterface ctx) {
		if (this.pluginManager != null) this.pluginManager.addService(serviceType, className, ctx);
	}
	public void sendJavascript(String statement) {
		if (this.callbackServer != null) this.callbackServer.sendJavascript(statement);
	}
	public void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params) { 
		if (clearHistory) this.clearHistory();
		if (!openExternal) {
			if (url.startsWith("file://") || url.indexOf(this.baseUrl) == 0 || isUrlWhiteListed(url)) {
				if (clearHistory) this.urls.clear();
				this.loadUrl(url);
			} else {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					this.startActivity(intent);
				} catch (android.content.ActivityNotFoundException e) {}
			}
		} else {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				this.startActivity(intent);
			} catch (android.content.ActivityNotFoundException e) {}
		}
	}
	public void endActivity() {
		//this.doUnbindService();
		this.activityState = ACTIVITY_EXITING;
		this.finish();
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (this.appView == null) return super.onKeyUp(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (this.bound) {
				this.sendJavascript("if($||false) $(window).triggerHandler('back_button');");
				return true;
			} else {
				if (this.backHistory()) return true;
				else {
					this.activityState = ACTIVITY_EXITING;
					return super.onKeyUp(keyCode, event);
				}
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_MENU) {
			this.sendJavascript("if($||false) $(window).triggerHandler('menu_button');");
			return super.onKeyUp(keyCode, event);
		}
		else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			this.sendJavascript("if($||false) $(window).triggerHandler('search_button');");
			return true;
		}
		return false;
	}
	@Override
	public void startActivityForResult(Intent intent, int requestCode) throws RuntimeException {
		super.startActivityForResult(intent, requestCode);
	}
	public void startActivityForResult(IPlugin command, Intent intent, int requestCode) {
		this.activityResultCallback = command;
		this.activityResultKeepRunning = this.keepRunning;
		if (command != null) this.keepRunning = false;
		super.startActivityForResult(intent, requestCode);
	}
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		IPlugin callback = this.activityResultCallback;
		if (callback != null) callback.onActivityResult(requestCode, resultCode, intent);
	}
	public void setActivityResultCallback(IPlugin plugin) { this.activityResultCallback = plugin; }
	public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
		final DroidGap me = this;
		final String errorUrl = me.getStringProperty("errorUrl", null);
		if ((errorUrl != null)
				&& (errorUrl.startsWith("file://") || errorUrl.indexOf(me.baseUrl) == 0 || isUrlWhiteListed(errorUrl))
				&& (!failingUrl.equals(errorUrl))) {
			me.runOnUiThread(new Runnable() {
				public void run() { me.showWebPage(errorUrl, false, true, null); }
			});
		} else {
			final boolean exit = !(errorCode == WebViewClient.ERROR_HOST_LOOKUP);
			me.runOnUiThread(new Runnable() {
				public void run() {
					if (exit) {
						me.appView.setVisibility(View.GONE);
						me.displayError("Application Error", description + " (" + failingUrl + ")", "OK", exit);
					}
				}
			});
		}
	}
	public void displayError(final String title, final String message, final String button, final boolean exit) {
		final DroidGap me = this;
		me.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder dlg = new AlertDialog.Builder(me);
				dlg.setMessage(message);
				dlg.setTitle(title);
				dlg.setCancelable(false);
				dlg.setPositiveButton(button,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								if (exit) me.endActivity();
							}
						});
				dlg.create();
				dlg.show();
			}
		});
	}
	private void loadConfiguration() {
		int id = getResources().getIdentifier("phonegap", "xml", getPackageName());
		if (id == 0) return;
		XmlResourceParser xml = getResources().getXml(id);
		int eventType = -1;
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				String strNode = xml.getName();
				if (strNode.equals("access")) {
					String origin = xml.getAttributeValue(null, "origin");
					String subdomains = xml.getAttributeValue(null, "subdomains");
					if (origin != null)	this.addWhiteListEntry(origin, (subdomains != null) && (subdomains.compareToIgnoreCase("true") == 0));
				} else if (strNode.equals("log")) {
					String level = xml.getAttributeValue(null, "level");
					if (level != null) LOG.setLogLevel(level);
				} else if (strNode.equals("preference")) {
					String name = xml.getAttributeValue(null, "name");
					String value = xml.getAttributeValue(null, "value");
					String readonlyString = xml.getAttributeValue(null,"readonly");
					boolean readonly = (readonlyString != null && readonlyString.equals("true"));
					preferences.add(new PreferenceNode(name, value, readonly));
				}
			}
			try { eventType = xml.next(); } catch (XmlPullParserException e){ e.printStackTrace(); } catch (IOException e){ e.printStackTrace(); }
		}
	}
	public void addWhiteListEntry(String origin, boolean subdomains) {
		try {
			if (origin.compareTo("*") == 0) whiteList.add(Pattern.compile(".*"));
			else {
				if (subdomains) {
					if (origin.startsWith("http")) 
						whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://(.*\\.)?")));
					else 
						whiteList.add(Pattern.compile("^https?://(.*\\.)?"+ origin));
				} else {
					if (origin.startsWith("http")) 
						whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://")));
					else whiteList.add(Pattern.compile("^https?://" + origin));
				}
			}
		} catch (Exception e) {}
	}
	public boolean isUrlWhiteListed(String url) {
		if (whiteListCache.get(url) != null) return true;
		Iterator<Pattern> pit = whiteList.iterator();
		while (pit.hasNext()) {
			Pattern p = pit.next();
			Matcher m = p.matcher(url);
			if (m.find()) {
				whiteListCache.put(url, true);
				return true;
			}
		}
		return false;
	}
	public String peekAtUrlStack() {
		if (urls.size() > 0) return urls.peek();
		return "";
	}
	public void pushUrl(String url) { urls.push(url); }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.postMessage("onCreateOptionsMenu", menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.postMessage("onPrepareOptionsMenu", menu);
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		this.postMessage("onOptionsItemSelected", item);
		return true;
	}
	public Context getContext() { return this; }
	public void bindBackButton(boolean override) { this.bound = override; }
	public boolean isBackButtonBound() { return this.bound; }
	public static String replaceJSSensitive(String i, boolean isUrl) 
	{
		String s;
		if(isUrl) {
			s = i
				.replace(",","%2c")
				.replace("'","%27")
				.replace("(","")
				.replace(")","")
				;
		} else {
			s = TextUtils
				.htmlEncode(i)
				.replace(",","&#8218;")
				.replace("'","&#39;")
				.replace("("," ")
				.replace(")"," ")
				;
		}
		return s;
	}
	public void initApp() {
    	this.addWhiteListEntry(this.getString(R.string.api_url), true);
		this.sendJavascript("$.app.init('"+
			replaceJSSensitive(this.getString(R.string.api_url),true) + 
			"'," +
			replaceJSSensitive(this.getString(R.string.loading_timeout),false) + 
			"," +
			(this.getString(R.string.app_mode).toLowerCase().contains("debug") ? "true" : "false") +
			"," +
			(this.getString(R.string.app_show_title).toLowerCase().contains("true") ? "true" : "false") +
			"," +
			(this.getString(R.string.app_show_status).toLowerCase().contains("true") ? "true" : "false") +
			",'" +
			replaceJSSensitive(this.getString(R.string.app_description),false) + 
			"','" +
			replaceJSSensitive(this.getString(R.string.app_default_status),false) + 
			"','" +
			replaceJSSensitive(this.getString(R.string.loading_message),false) + 
			"','" +
			replaceJSSensitive(this.getString(R.string.loading_err_message),false) +
			"');"
		);
	}
	//public class IncomingHandler extends Handler {
		//@Override
		//public void handleMessage(Message msg) {
			//switch (msg.what) {
			//case PhoneGapService.MSG_SET_VALUE:
				//LOG.i(TAG, "Registered to use Service...");
				//break;
			//default:
				//super.handleMessage(msg);
			//}
		//}
	//}
	//private ServiceConnection mConnection = new ServiceConnection() {
		//public void onServiceConnected(ComponentName className, IBinder iservice) {
			//mService = new Messenger(iservice);
			//try {
				//Message msg = Message.obtain(null, PhoneGapService.MSG_REGISTER_CLIENT);
				//msg.replyTo = mMessenger;
				//mService.send(msg);
				//msg = Message.obtain(null, PhoneGapService.MSG_SET_VALUE, this.hashCode(), 0);
				//mService.send(msg);
				//msg = Message.obtain(null, PhoneGapService.MSG_SET_NOTIFY);
				//Bundle b = new Bundle();
				//b.putString("message", "App Ready...");
				//msg.setData(b);
				//mService.send(msg);
			//} catch (RemoteException e) {
				//LOG.i(TAG, "Service error : " + e.getMessage());
			//}
			//LOG.i(TAG, "Service conected");
		//}

		//public void onServiceDisconnected(ComponentName name) {
			//mService = null;
			//LOG.i(TAG, "Service disconected");
		//}
	//};

	//void doBindService() {
		//LOG.i(TAG, "Starting service");
		//bindService(new Intent(this, PhoneGapService.class), mConnection,
				//Context.BIND_AUTO_CREATE);
		//mIsBound = true;
	//}

	//void doUnbindService() {
		//LOG.i(TAG, "Stopping service");
		//if (mIsBound) {
			//if (mService != null) {
				//try {
					//Message msg = Message.obtain(null, PhoneGapService.MSG_UNREGISTER_CLIENT);
					//msg.replyTo = mMessenger;
					//mService.send(msg);
				//} catch (RemoteException e) {
				//}
			//}
			//unbindService(mConnection);
			//mIsBound = false;
		//}
	//}
}