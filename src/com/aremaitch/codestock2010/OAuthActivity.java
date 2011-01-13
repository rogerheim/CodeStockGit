/*
 * Copyright 2010-2011 Roger Heim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.aremaitch.codestock2010;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.RequestToken;

import com.aremaitch.utils.AndroidOAuth;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthActivity extends Activity {

	public final int AUTHORIZE_TO_TWITTER = 1;
	public final int AUTHORIZE_TO_FOURSQUARE = 2;
	
	SharedPreferences prefs;
	WebView wv;
	Twitter t;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.authentication_activity);

	    t = new TwitterFactory().getInstance();
	    t.setOAuthConsumer(getString(R.string.test_platform_oauth_key), getString(R.string.test_platform_oauth_secret));
	    try {
			RequestToken reqT = t.getOAuthRequestToken(getString(R.string.twitter_callback_uri));
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Intent i = this.getIntent();
	    if (i.getData() == null) {
	    	// Not called via callback

	    	wv = (WebView)findViewById(R.id.auth_webview);
	    	wv.setWebViewClient(new AuthenticationClient());
//	    	wv.loadUrl(authUrl);
	    }
	}
	
	
	class AuthenticationClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			
			Uri uri = Uri.parse(url);
//			if (uri != null && androidOAuth.getCallbackUri().getScheme().equals(uri.getScheme())) {
//				
//			}
			return super.shouldOverrideUrlLoading(view, url);
		}
	}

}
