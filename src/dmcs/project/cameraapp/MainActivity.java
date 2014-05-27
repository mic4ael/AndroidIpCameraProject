package dmcs.project.cameraapp;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import dmcs.project.cameraapp.mjpeg.MJPegInputStream;
import dmcs.project.cameraapp.mjpeg.MJPegView;

public class MainActivity extends ActionBarActivity {
	private static final String url = "http://212.51.218.248/video.cgi";
	private MJPegView mv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mv = new MJPegView(this);
		setContentView(mv);
		
		new DoRead().execute(url);
	}
	
	public class DoRead extends AsyncTask<String, Void, MJPegInputStream> {

		@Override
		protected MJPegInputStream doInBackground(String... url) {
			HttpResponse res = null;
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getCredentialsProvider().setCredentials(
					   new AuthScope("https://212.51.218.248/", AuthScope.ANY_PORT), 
					   new UsernamePasswordCredentials("root", "test.123"));
			
			try {
				res = httpClient.execute(new HttpGet(URI.create(url[0])));
				if (res.getStatusLine().getStatusCode() == 401) {
					return null;
				}
				
				return new MJPegInputStream(res.getEntity().getContent());
			} catch (ClientProtocolException ex) {
				Log.d(this.getClass().toString(), ex.toString());
			} catch (IOException ex) {
				Log.d(this.getClass().toString(), ex.toString());
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(MJPegInputStream result) {
			mv.setSource(result);
			mv.setDispMode(MJPegView.SIZE_BEST_FIT);
			mv.setShowFps(true);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mv.stopPlayback();
	}
}
