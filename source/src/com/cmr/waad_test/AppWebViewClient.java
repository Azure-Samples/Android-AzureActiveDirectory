package com.cmr.waad_test;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AppWebViewClient extends WebViewClient {
	
	private Context mContext;
	
	public AppWebViewClient(Context context) {
		mContext = context;
	}
	
	@Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {		
		//Trigger this activity to finish when we hit the REDIRECT_URL
		if (url.startsWith(Constants.REDIRECT_URL)) {
			WebViewActivity activity = (WebViewActivity) mContext;
			activity.completeWithCode(url);
			return true;
		}
		
		view.loadUrl(url);
		return false;
    }
}
