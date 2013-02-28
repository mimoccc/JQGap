package eu.mjdev.phonegap;

import eu.mjdev.app.R;
import eu.mjdev.phonegap.api.LOG;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HttpCacheDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    
    private static final String TABLE_NAME   = "http";
    private static final String KEY_NAME     = "url";
    private static final String KEY_DATA     = "data";
    
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY_NAME + " TEXT, " + KEY_DATA + " TEXT);";

    HttpCacheDBHelper(Context context) {
    	super(context, context.getString(R.string.app_name), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { 
    	db.execSQL(DICTIONARY_TABLE_CREATE); 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if(newVersion>oldVersion) {
    		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
    	}
    }
    
    public void addHttpItem(String url, String data) {
    	LOG.d("HTTPHELPER","HTTP DB ADD ITEM");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, url);
        values.put(KEY_DATA, data);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    
    public String getHttpItem(String url) {
    	String ret = null;
    	//try{
    		//SQLiteDatabase db = this.getReadableDatabase();
    		//Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_NAME, KEY_DATA }, KEY_NAME + "=?", new String[] { url }, null, null, null, null);
    		//if (cursor != null) ret = cursor.getString(1);
    		//db.close();
    	//} catch (Exception e) {}
        return ret;
    }
}