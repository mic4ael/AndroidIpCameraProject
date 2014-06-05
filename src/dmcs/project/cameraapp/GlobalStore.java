package dmcs.project.cameraapp;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class GlobalStore {
	public static final String url = "http://212.51.218.248/still.jpg";
	private static DefaultHttpClient httpClient;
	
	public static DefaultHttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope("212.51.218.248", AuthScope.ANY_PORT), 
					new UsernamePasswordCredentials("root", "test.123"));
		}
		
		return httpClient;
	}
}
