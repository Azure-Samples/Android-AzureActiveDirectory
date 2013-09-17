package com.cmr.waad_test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	private MainActivity mContext;
	private Button mBtnAuthenticate;
	private Button mBtnGetToken;
	private Button mBtnFetchItems;
	private Button mBtnAddItem;
	private Button mBtnDecodeClaims;
	private TextView mLblInfo;	
	private TextView mLblUser;
	private EditText mTxtTodo;
	
	private StringBuilder mTodoItems;
	
	private String mAuthCode;
	private String mAccessToken;
	private String mExpiresIn;
	private String mExpiresOn;
	private String mIdToken;
	private String mRefreshToken;
	private String mResource;
	private String mScope;
	private String mTokenType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		
		mBtnAuthenticate = (Button) findViewById(R.id.btnAuthenticate);
		mBtnGetToken = (Button) findViewById(R.id.btnGetToken);
		mBtnFetchItems = (Button) findViewById(R.id.btnFetchItems);
		mBtnAddItem = (Button) findViewById(R.id.btnAddItem);
		mBtnDecodeClaims = (Button) findViewById(R.id.btnDecodeClaims);
		mLblInfo = (TextView) findViewById(R.id.lblInfo);
		mLblUser = (TextView) findViewById(R.id.lblUser);
		mTxtTodo = (EditText) findViewById(R.id.txtTodo);
		
		mBtnAuthenticate.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), WebViewActivity.class);				
				startActivityForResult(intent, Constants.AuthenticateRequestCode);
			}
		});
		
		mBtnGetToken.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				new FetchAuthTokenTask(mContext).execute();
			}
		});
		
		mBtnFetchItems.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				new FetchTodoItemsTask(mContext).execute();
			}
		});
		
		mBtnAddItem.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				new AddTodoItemTask(mContext).execute();
			}
		});
		
		mBtnDecodeClaims.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				decodeClaims();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Handles the result back from authenticating using the web view
		if (requestCode == Constants.AuthenticateRequestCode) {
			String url = data.getStringExtra(Constants.EXTRA_CODE_URL);
			Log.i("MainActivity", "Result: " + url);
			if (url.contains("code=")) {
				Uri uri = Uri.parse(url);
				mAuthCode = uri.getQueryParameter("code");
				mBtnGetToken.setEnabled(true);
				mBtnFetchItems.setEnabled(false);
                mBtnAddItem.setEnabled(false);
                mBtnDecodeClaims.setEnabled(false);
                mLblInfo.setText("Auth code is " + mAuthCode);
			}
		}		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void decodeClaims() {
		//Get claims out of access token (content between 2nd and 3rd periods)
		int firstIndex = mAccessToken.indexOf(".");
		int secondIndex = mAccessToken.indexOf(".", firstIndex+2);
		String claims = mAccessToken.substring(firstIndex + 1, secondIndex);
		//Decode base64 URL ended claims
		byte[] data = Base64.decode(claims, Base64.URL_SAFE);
		try {
			String text = new String(data, "ASCII");
			//Display claims on screen
			mLblInfo.setText(text);
			JSONObject jObject = new JSONObject(text);
			//Get and display the logged in user name
			mLblUser.setText("Logged in as " + jObject.getString("unique_name"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(TAG, "Error decoding claims: " + e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "Error converting to json: " + e.getMessage());
		}
		
	}
	
	/*
	 * Background task to handle fetching an auth token
	 */
	private class FetchAuthTokenTask extends AsyncTask<Void, Void, Integer> {		
		private MainActivity mContext;
		
		public FetchAuthTokenTask(MainActivity context) {
			mContext = context;
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			HttpURLConnection urlConnection = null;
			try {
				URL tokenUrl = new URL(Constants.LOGIN_URL + Constants.DOMAIN +"/oauth2/token");
				urlConnection = (HttpURLConnection) tokenUrl.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				urlConnection.setRequestMethod("POST");
				urlConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				urlConnection.addRequestProperty("Accept", "application/json");
				
				//Create our key-value pairs to send to the server
				String parameters = "client_id=" + Constants.CLIENT_ID +
								   "&code=" + mAuthCode +
								   "&grant_type=authorization_code" +
								   "&redirect_uri=" + Constants.REDIRECT_URL;
				//Open an output stream and write the data to the server
				DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
				wr.write(parameters.getBytes());
				wr.flush();
				wr.close();
				int response = urlConnection.getResponseCode();
				//Get response stream
				InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder stringBuilderResult = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilderResult.append(line);
                }
                //Deserialize the data into JSON
                JSONObject statusObject = new JSONObject(stringBuilderResult.toString());
                //Pull values out of the JSON
                mAccessToken = statusObject.getString("access_token");
                Log.i(TAG, "Access Token: " + mAccessToken);
                mExpiresIn = statusObject.getString("expires_in");
                mExpiresOn = statusObject.getString("expires_on");
                mIdToken = statusObject.getString("id_token");
                mRefreshToken = statusObject.getString("refresh_token");
                mResource = statusObject.getString("resource");
                mScope = statusObject.getString("scope");
                mTokenType = statusObject.getString("token_type");
				return 1;
			} catch (Exception ex) {
				Log.e(TAG, "Error fetching token: " + ex.getMessage());
				return -1;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			//Update the UI with the results
			mLblInfo.setText("Result is : " + result);			
			if (result == 1) {
				mBtnFetchItems.setEnabled(true);
                mBtnAddItem.setEnabled(true);
                mBtnDecodeClaims.setEnabled(true);
                mTxtTodo.setEnabled(true);
                mLblInfo.setText("Access Token: " + mAccessToken);
			} else {				
				mBtnFetchItems.setEnabled(false);
                mBtnAddItem.setEnabled(false);
                mBtnDecodeClaims.setEnabled(false);
                mTxtTodo.setEnabled(false);
                mLblInfo.setText("Issue fetching access token, check debug logs");
			}
		}
	}
	
	/*
	 * Background task to handle fetching todo items from our secured service
	 */
	private class FetchTodoItemsTask extends AsyncTask<Void, Void, Integer> {		
		private MainActivity mContext;
		
		public FetchTodoItemsTask(MainActivity context) {
			mContext = context;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			HttpURLConnection urlConnection = null;
			try {
				URL tokenUrl = new URL(Constants.RESOURCE + Constants.SERVICE_ENDPOINT);
				urlConnection = (HttpURLConnection) tokenUrl.openConnection();
				urlConnection.setDoInput(true);
				urlConnection.setRequestMethod("GET");
				urlConnection.addRequestProperty("Accept", "application/json");
				//Set the Authorzation header:  Bearer <token>
				urlConnection.addRequestProperty("Authorization", "Bearer " + mAccessToken);				
				int response = urlConnection.getResponseCode();
				//Get the response stream and read it out
				InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder stringBuilderResult = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilderResult.append(line);
                }
                Log.w(TAG, "Data response: " + stringBuilderResult.toString());
                //Convert response to JSON
                JSONArray jsonArray = new JSONArray(stringBuilderResult.toString());
                mTodoItems = new StringBuilder();
                //Loop through each todo item and pull out it's title
                for (int i = 0; i < jsonArray.length(); i++) {
                		JSONObject jObject = jsonArray.getJSONObject(i);
                		mTodoItems.append(jObject.getString("Title"));
                		mTodoItems.append(",");
                }                               	
				return 1;
			} catch (Exception ex) {
				Log.e(TAG, "Error fetching todo items: " + ex.getMessage());
				return -1;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			//Update the UI
			if (result == 1) {
				mLblInfo.setText("todo items found: " + mTodoItems.toString());
			} else {
				mLblInfo.setText("Issue fetching todo items, check debug logs");
			}
		}
	}
	
	/*
	 * Background task for saving a todo item
	 */
	private class AddTodoItemTask extends AsyncTask<Void, Void, Integer> {		
		private MainActivity mContext;
		
		public AddTodoItemTask(MainActivity context) {
			mContext = context;
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			HttpURLConnection urlConnection = null;
			try {
				URL tokenUrl = new URL(Constants.RESOURCE + Constants.SERVICE_ENDPOINT);
				urlConnection = (HttpURLConnection) tokenUrl.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("POST");
				urlConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				urlConnection.addRequestProperty("Authorization", "Bearer " + mAccessToken);			
				
				String parameters = "Title=" + mTxtTodo.getText().toString();
				//Get the output stream and write our todo item data to it
				DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
				wr.write(parameters.getBytes());
				wr.flush();
				wr.close();
				int response = urlConnection.getResponseCode();				
				Log.i(TAG, "Success with code: " + response);    
				return 1;
			} catch (Exception ex) {
				Log.e(TAG, "Error fetching token: " + ex.getMessage());
				return -1;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			//Update our UI
			if (result == 1) {			
                mLblInfo.setText("New item added");
			} else {				
                mLblInfo.setText("Issue adding item");
			}
		}
	}
}
