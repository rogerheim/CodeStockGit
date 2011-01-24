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

import android.content.Context;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.widget.TextView;
import com.aremaitch.codestock2010.library.CSConstants;
import com.aremaitch.codestock2010.library.TwitterConstants;
import com.aremaitch.codestock2010.library.TwitterOAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.RequestToken;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthActivity extends Activity {

	SharedPreferences prefs;
	WebView wv;
	TwitterOAuth toa = null;

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, OAuthActivity.class);
        ctx.startActivity(i);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.authentication_activity);
        setHeaderText();

	    toa = new TwitterOAuth();
//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs = getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Intent i = this.getIntent();
        if (i.getData() == null) {
            try {
                if (toa.authenticateToTwitter(this.getString(R.string.twitter_oauth_key), this.getString(R.string.twitter_oauth_secret), TwitterConstants.OAUTH_CALLBACK_URL)) {
                    wv = (WebView)findViewById(R.id.auth_webview);
                    wv.setWebViewClient(new AuthenticationClient());
                    wv.loadUrl(toa.getAuthorizationURL());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
	}
	
	private void setHeaderText() {
        ((TextView)findViewById(R.id.header_title)).setText(getString(R.string.header_title));
        ((TextView)findViewById(R.id.header_subtitle)).setText(getString(R.string.header_slogan));
    }

	class AuthenticationClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			
			Uri uri = Uri.parse(url);
            if (uri != null && uri.getScheme().equalsIgnoreCase(Uri.parse(TwitterConstants.OAUTH_CALLBACK_URL).getScheme())) {

                toa.saveOAuthTokens(prefs, toa.getAccessToken(), toa.getTokenSecret(), toa.getTwitterUserScreenName());
                OAuthActivity.this.finish();
            } else {
                view.loadUrl(url);
            }
			return true;
		}

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

}
