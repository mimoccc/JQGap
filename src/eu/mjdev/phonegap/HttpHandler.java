package eu.mjdev.phonegap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import eu.mjdev.phonegap.api.LOG;
import eu.mjdev.phonegap.api.PhoneGapInterface;
import android.os.Environment;

public class HttpHandler 
{
	private PhoneGapInterface ctx;
	
    public HttpHandler(PhoneGapInterface ctx) { this.ctx = ctx; }
    
	public Boolean get(String url, String file) throws ClientProtocolException, IOException
	{
		HttpEntity entity = getHttpEntity(url);
		try {
			writeToDisk(entity, file);
		} catch (Exception e) { e.printStackTrace(); return false; }
		try {
			entity.consumeContent();
		} catch (Exception e) { e.printStackTrace(); return false; }
		return true;
	}
	
	public String getCached(String url) throws IllegalStateException, IOException {
		String res = null;
		if(this.ctx != null) {
			HttpCacheDBHelper cache = ((PhoneGapInterface) this.ctx).getHTTPCache();
			res = cache.getHttpItem(url);
			if(res == null) {
				HttpEntity entity = getHttpEntity(url);
				if(entity != null) {
					//cache.addHttpItem(url, res);
					res = EntityUtils.toString(entity, HTTP.UTF_8);
					LOG.d("TEST", res);
				}
			}
		}
		return res;
	}

	public HttpEntity getHttpEntity(String url) throws ClientProtocolException, IOException
	{
		HttpEntity entity = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);
		entity = response.getEntity();
		return entity;
	}
	
	public void writeToDisk(HttpEntity entity, String file) throws IllegalStateException, IOException
	{  
		String sdcardPath = Environment.getExternalStorageDirectory().getPath();
		String FilePath   = sdcardPath + this.ctx.getPackageName() + "/" + file;
		InputStream in = entity.getContent();
		byte buff[] = new byte[1024];    
		FileOutputStream out = new FileOutputStream(FilePath);
		do {
			int numread = in.read(buff);
			if (numread <= 0) break;
			out.write(buff, 0, numread);
		} while (true);
		out.flush();
		out.close();	
	}
}