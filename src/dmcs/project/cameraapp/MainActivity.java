package dmcs.project.cameraapp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import dmcs.project.cameraapp.mjpeg.MJPegInputStream;
import dmcs.project.cameraapp.mjpeg.MJPegView;

public class MainActivity extends ActionBarActivity {
	private MJPegView mv;
	private final static String CONFIG_FILE = "config.txt";
	private static final String KEY = "Encrypt";
	private final static int SELECT_PICTURE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mv = new MJPegView(this);
		setContentView(mv);

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.setTitle("Connect to...");

		final EditText inputAddr = (EditText) dialog
				.findViewById(R.id.input_address);
		final EditText inputUser = (EditText) dialog
				.findViewById(R.id.input_user_name);
		final EditText inputPass = (EditText) dialog
				.findViewById(R.id.input_password);
		
		if (!readDataFromFile().equals("")) {
			String lines[] = readDataFromFile().split("\\r?\\n");
			inputAddr.setText(lines[0].replaceAll("\\s+", ""));
			inputUser.setText(lines[1].replaceAll("\\s+", ""));
			inputPass.setText(decryptString(lines[2]));
		}

		Button button = (Button) dialog.findViewById(R.id.dialogButtonOK);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				CheckBox saveCheckBox = (CheckBox) dialog
						.findViewById(R.id.save_checkbox);
				
				if (!inputAddr.getText().toString().equals("") && 
					!inputUser.getText().toString().equals("")) {
					
					GlobalStore.setAddr(inputAddr.getText().toString());
					GlobalStore.setUser(inputUser.getText().toString());
					GlobalStore.setPasswd(inputPass.getText().toString());
					
					if (saveCheckBox.isChecked()) {
						saveDataToFile();
					}
					
					try {
						if (new DoRead().execute(GlobalStore.getAddr()).get() == null) {
							Toast.makeText(MainActivity.this, "Connection problem!", Toast.LENGTH_LONG).show();
						} else {
							dialog.dismiss();
						}
					} catch (InterruptedException | ExecutionException e) {
						Toast.makeText(MainActivity.this, "Connection problem!", Toast.LENGTH_LONG).show();
					}
					
				} else {
					Toast.makeText(MainActivity.this, "No credentials!", Toast.LENGTH_LONG).show();
				}
			}
		});

		dialog.show();
	}

	public class DoRead extends AsyncTask<String, Void, MJPegInputStream> {

		@Override
		protected MJPegInputStream doInBackground(String... url) {
			HttpResponse res = null;

			try {
				res = GlobalStore.getHttpClient().execute(
						new HttpGet(URI.create(url[0])));
				if (res.getStatusLine().getStatusCode() == 401) {
					GlobalStore.clearAll();
					return null;
				}
				
				return new MJPegInputStream(res.getEntity().getContent());
			} catch (ClientProtocolException ex) {
				Toast.makeText(MainActivity.this, "Connection Error!", Toast.LENGTH_LONG).show();
			} catch (IOException ex) {
				Toast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
			}

			return null;
		}

		@Override
		protected void onPostExecute(MJPegInputStream result) {
			if (result != null) {
				mv.setDispMode(MJPegView.SIZE_BEST_FIT);
				mv.setShowFps(true);
				mv.setSource(result);
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();

	}
	
	@Override
	public void onResume() {
		super.onResume();
		mv.startPlayback();
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
		if (id == R.id.action_stop) {
			mv.stopPlayback();
			return true;
		}
		
		if (id == R.id.action_start) {
			mv.startPlayback();
			return true;
		}
		
		if (id == R.id.action_exit) {
			Process.killProcess(Process.myPid());
			return true;
		}
		
		if (id == R.id.action_gallery) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
									 Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			startActivityForResult(intent, SELECT_PICTURE);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Log.d("##PICKED FOTO:", "SELECT_PICTURE");
                
                Uri selectedImageUri = data.getData();
                
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(selectedImageUri, "image/png");
                startActivity(intent);
            }
        }
    }

	@Override
	protected void onPause() {
		super.onPause();
		mv.stopPlayback();
	}

	private void saveDataToFile() {
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(CONFIG_FILE, Context.MODE_PRIVATE);
			outputStream.write((GlobalStore.getAddr() + "\n"
					+ GlobalStore.getUser() + "\n" + encryptString(GlobalStore.getPasswd()))
					.getBytes());
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String readDataFromFile() {
		String ret = "";

		try {
			InputStream inputStream = openFileInput(CONFIG_FILE);

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString + "\n");
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		}

		return ret;
	}

	private static String encryptString(String str) {
		StringBuffer sb = new StringBuffer(str);

		int lenStr = str.length();
		int lenKey = KEY.length();

		for (int i = 0, j = 0; i < lenStr; i++, j++) {
			if (j >= lenKey)
				j = 0; 
			
			sb.setCharAt(i, (char) (str.charAt(i) ^ KEY.charAt(j)));
		}

		return sb.toString();
	}

	private static String decryptString(String str) {
		return encryptString(str);
	}
}
