package io.prediction.samples.androidclient;

import io.prediction.Client;

import org.OpenUDID.OpenUDID_manager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	// The best practice is to use only one Client per app
	// Each Client object manages its own reusable thread pool
	private Client client;

	// Get status async task
	// Must use async task to avoid networking on main thread exception
	private class StatusTask extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... v) {
			EditText appKey = (EditText) findViewById(R.id.app_key);
			EditText apiUrl = (EditText) findViewById(R.id.api_url);
			client.setAppkey(appKey.getText().toString());
			client.setApiUrl(apiUrl.getText().toString());
			String status = "";
			try {
				status = client.getStatus();
			} catch (Exception e) {
				status = ExceptionUtils.getStackTrace(e);
			}
			return status;
		}

		protected void onPostExecute(String status) {
			TextView console = (TextView) findViewById(R.id.console_output);
			console.setText(status);
		}
	}

	// Get recommendations async task
	private class RecsTask extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... v) {
			EditText appKey = (EditText) findViewById(R.id.app_key);
			EditText apiUrl = (EditText) findViewById(R.id.api_url);
			EditText engine = (EditText) findViewById(R.id.engine);
			EditText uid = (EditText) findViewById(R.id.uid);
			EditText n = (EditText) findViewById(R.id.n);
			client.setAppkey(appKey.getText().toString());
			client.setApiUrl(apiUrl.getText().toString());
			String result = "";
			try {
				String[] iids = client.getItemRecTopN(engine.getText()
						.toString(), uid.getText().toString(), Integer
						.parseInt(n.getText().toString()));
				result = StringUtils.join(iids, ",");
			} catch (Exception e) {
				result = ExceptionUtils.getStackTrace(e);
			}
			return result;
		}

		protected void onPostExecute(String result) {
			TextView console = (TextView) findViewById(R.id.console_output);
			console.setText(result);
		}
	}

	// Log a user-view-item action
	private class SaveViewTask extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... v) {
			EditText appKey = (EditText) findViewById(R.id.app_key);
			EditText apiUrl = (EditText) findViewById(R.id.api_url);
			EditText uid = (EditText) findViewById(R.id.view_uid);
			EditText iid = (EditText) findViewById(R.id.view_iid);
			client.setAppkey(appKey.getText().toString());
			client.setApiUrl(apiUrl.getText().toString());
			String result = "";
			try {
				String suid = uid.getText().toString();
				String siid = iid.getText().toString();
				client.userViewItem(suid, siid);
				result = "Logged UID "+suid+" view IID "+siid;
			} catch (Exception e) {
				result = ExceptionUtils.getStackTrace(e);
			}
			return result;
		}

		protected void onPostExecute(String result) {
			TextView console = (TextView) findViewById(R.id.console_output);
			console.setText(result);
		}
	}

	// Log a user-view-item action
	private class OpenUDIDTask extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... v) {
			try {
				while (!OpenUDID_manager.isInitialized()) {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				return ExceptionUtils.getStackTrace(e);
			}
			return OpenUDID_manager.getOpenUDID();
		}

		protected void onPostExecute(String result) {
			TextView console = (TextView) findViewById(R.id.openudid);
			console.setText("OpenUDID: "+result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		EditText apiUrl = (EditText) findViewById(R.id.api_url);
		EditText engine = (EditText) findViewById(R.id.engine);
		EditText uid = (EditText) findViewById(R.id.uid);
		EditText n = (EditText) findViewById(R.id.n);
		apiUrl.setText("http://api:8000");
		engine.setText("test");
		uid.setText("1");
		n.setText("10");
		OpenUDID_manager.sync(this);
		new OpenUDIDTask().execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EditText apiUrl = (EditText) findViewById(R.id.api_url);
		// Android 2.2 emulator workaround
		// You probably do not need this on real hardware
		// https://code.google.com/p/android/issues/detail?id=9431
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		client = new Client("", apiUrl.getText().toString(), 10);
	}

	@Override
	protected void onStop() {
		super.onStop();
		client.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Button click handlers
	public void getStatus(View view) {
		new StatusTask().execute();
	}

	public void getRecs(View view) {
		new RecsTask().execute();
	}

	public void saveView(View view) {
		new SaveViewTask().execute();
	}

}
