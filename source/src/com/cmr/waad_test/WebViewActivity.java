package com.cmr.waad_test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		//Create a web view and set it's client to our custom class
		WebView webView = new WebView(this);
		AppWebViewClient webViewClient = new AppWebViewClient(this);
		webView.setWebViewClient(webViewClient);
		//Turn on javascript (required for AD web Auth)
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);		
		//Load our webview URL
		webView.loadUrl(Constants.LOGIN_URL + 
						Constants.DOMAIN +
						"/oauth2/authorize?response_type=code&resource=" +
						Constants.RESOURCE + "&client_id=" +
						Constants.CLIENT_ID + "&redirect_uri=" + 
						Constants.REDIRECT_URL);
		//Display the web view
		setContentView(webView);
	}
	
	public void completeWithCode(String url) {
		//Callback for finishing this activity and returning the code
		Intent data = new Intent();
		data.putExtra(Constants.EXTRA_CODE_URL, url);			
		setResult(Constants.RESULT_CODE_AUTHENTICATE, data);
		finish();
	}
}
