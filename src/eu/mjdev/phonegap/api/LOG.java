package eu.mjdev.phonegap.api;

import android.util.Log;

public class LOG {
   
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    
    public static final String TAG = "PhoneGapLog";

    public static int LOGLEVEL = Log.VERBOSE;
    
    public static void setLogLevel(int logLevel) {
        LOGLEVEL = logLevel;
        Log.i(TAG, "Changing log level to " + logLevel);
    }
    
    public static void setLogLevel(String logLevel) {
        if ("VERBOSE".equals(logLevel)) LOGLEVEL = VERBOSE;
        else if ("DEBUG".equals(logLevel)) LOGLEVEL = DEBUG;
        else if ("INFO".equals(logLevel)) LOGLEVEL = INFO;
        else if ("WARN".equals(logLevel)) LOGLEVEL = WARN;
        else if ("ERROR".equals(logLevel)) LOGLEVEL = ERROR;
        Log.i(TAG, "Changing log level to " + logLevel + "(" + LOGLEVEL + ")");
    }

    public static boolean isLoggable(int logLevel) { return (logLevel >= LOGLEVEL); }
    public static void v(String tag, String s) { if (LOG.VERBOSE >= LOGLEVEL) Log.v(tag, s); }
    public static void d(String tag, String s) { if (LOG.DEBUG >= LOGLEVEL) Log.d(tag, s); }
    public static void i(String tag, String s) { if (LOG.INFO >= LOGLEVEL) Log.i(tag, s); }
    public static void w(String tag, String s) { if (LOG.WARN >= LOGLEVEL) Log.w(tag, s); }
    public static void e(String tag, String s) { if (LOG.ERROR >= LOGLEVEL) Log.e(tag, s); }
    public static void v(String tag, String s, Throwable e) { if (LOG.VERBOSE >= LOGLEVEL) Log.v(tag, s, e); }
    public static void d(String tag, String s, Throwable e) { if (LOG.DEBUG >= LOGLEVEL) Log.d(tag, s, e); }
    public static void i(String tag, String s, Throwable e) { if (LOG.INFO >= LOGLEVEL) Log.i(tag, s, e); }
    public static void w(String tag, String s, Throwable e) { if (LOG.WARN >= LOGLEVEL) Log.w(tag, s, e); }
    public static void e(String tag, String s, Throwable e) { if (LOG.ERROR >= LOGLEVEL) Log.e(tag, s, e); }
    public static void v(String tag, String s, Object... args) { if (LOG.VERBOSE >= LOGLEVEL) Log.v(tag, String.format(s, args)); }
    public static void d(String tag, String s, Object... args) { if (LOG.DEBUG >= LOGLEVEL) Log.d(tag, String.format(s, args)); }
    public static void i(String tag, String s, Object... args) { if (LOG.INFO >= LOGLEVEL) Log.i(tag, String.format(s, args)); }
    public static void w(String tag, String s, Object... args) { if (LOG.WARN >= LOGLEVEL) Log.w(tag, String.format(s, args)); }
    public static void e(String tag, String s, Object... args) { if (LOG.ERROR >= LOGLEVEL) Log.e(tag, String.format(s, args)); }

}