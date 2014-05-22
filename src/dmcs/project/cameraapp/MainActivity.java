package dmcs.project.cameraapp;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends ActionBarActivity {
	private VideoView videoView;
	private MediaController mediaController;
	private Uri cameraUri;
	private static final String url = "https://212.51.218.248/video.cgi";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try {
			initUrl();
			initVideoView();
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUrl() {
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("root", "test.123".toCharArray());
			}
		});
		
		cameraUri = Uri.parse(url);
	}

	private void initVideoView() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
		mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		videoView = (VideoView) findViewById(R.id.videoViewId);
		videoView.setVideoURI(cameraUri);
		videoView.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				videoView.start();
			}
		});
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
}
