package dmcs.project.cameraapp;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public abstract class GlobalStore {
	public static final String url = "http://212.51.218.248/still.jpg";
	private static DefaultHttpClient httpClient;
	private static String user;
	private static String addr;
	private static String passwd;

	public static void setUser(String u) {
		user = u;
	}

	public static String getUser() {
		return user;
	}

	public static void setAddr(String address) {
		addr = address;
	}

	public static String getAddr() {
		return addr;
	}

	public static void setPasswd(String pass) {
		passwd = pass;
	}

	public static String getPasswd() {
		return passwd;
	}

	public static DefaultHttpClient getHttpClient() throws MalformedURLException {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
			
			Log.d("#User: ", GlobalStore.getUser());
			Log.d("#Pass: ", GlobalStore.getPasswd());
			try {
				URL u = new URL(GlobalStore.getAddr());
				httpClient.getCredentialsProvider().setCredentials(
						new AuthScope(u.getHost(), AuthScope.ANY_PORT),
						new UsernamePasswordCredentials(GlobalStore.getUser(),
								GlobalStore.getPasswd()));
				Log.d("###URL AUTH:", u.getAuthority());
				Log.d("###URL HOST:", u.getHost());
			} catch (MalformedURLException e) {
				throw e;
			}
		}
		
		return httpClient;
	}
	
	public static void clearAll() {
		httpClient = null;
		addr = null;
		passwd = null;
		user = null;
	}
}