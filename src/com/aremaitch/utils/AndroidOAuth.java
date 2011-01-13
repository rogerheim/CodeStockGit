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

package com.aremaitch.utils;


import com.aremaitch.codestock2010.R;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;

//	http://code.google.com/p/androidoauth2/

public class AndroidOAuth {
	
	private String consumerKey;
	private String consumerSecret;
	private String accessTokenUrl;
	private String authenticationUrl;
	private boolean touchDisplay;
	
	private Uri callbackUri;
	
	private String accessTokenPreferenceName;
	private String savedAccessToken = null;
	
	public AndroidOAuth(Resources res) {
		this.consumerKey = res.getString(R.string.test_platform_oauth_key);
		this.consumerSecret = res.getString(R.string.test_platform_oauth_secret);
		this.accessTokenUrl = res.getString(R.string.twitter_access_token_url);
		this.authenticationUrl = res.getString(R.string.twitter_authorize_url);
		this.callbackUri = Uri.parse(res.getString(R.string.twitter_callback_uri));
		this.touchDisplay = Boolean.parseBoolean(res.getString(R.string.twitter_touch_display));
		this.accessTokenPreferenceName = res.getString(R.string.twitter_access_token_pref_name);
	}
	
	public Uri getCallbackUri() {
		return callbackUri;
	}
	
	public String getAuthenticationURL() {
		String touchParam = "";
		
		if (touchDisplay) {
			touchParam = "&display=touch";
		}

		//	This block returns the "code" response authentication request.
		
		return 
			new StringBuilder()
			.append(authenticationUrl)
			.append("?client_id=").append(consumerKey)
			.append("&response_type=code")
			.append("&redirect_uri=").append(callbackUri)
			.append(touchParam)
			.toString();

		//	This block returns the "token" response authentication request.
		
//		return
//			new StringBuilder()
//				.append(authenticationUrl)
//				.append("?client_id=").append(consumerKey)
//				.append("&response_type=token")
//				.append("&redirect_uri=").append(callbackUri)
//				.append(touchParam)
//				.toString();
	}
	
	public String getAccessTokenUrl(String code) {
		String touchParam = (touchDisplay == true) ? "&display=touch" : "";
		
		return
			new StringBuilder()
				.append(accessTokenUrl)
				.append("?client_id=").append(consumerKey)
				.append("&client_secret=").append(consumerSecret)
				.append("&grant_type=authorization_code")
				.append("&redirect_uri=").append(callbackUri)
				.append("&code=").append(code)
				.append(touchParam)
				.toString();
	}
	
	public String getAccessTokenPreferenceName() {
		return accessTokenPreferenceName;
	}
	
	public void saveAccessToken(SharedPreferences preferences, String accessToken) {
		SharedPreferences.Editor editor = preferences.edit();
		
		if (accessToken == null) {
			editor.remove(accessTokenPreferenceName);
		} else {
			editor.putString(accessTokenPreferenceName, accessToken);
		}
		editor.commit();
	}
	
	public String getSavedAccessToken(SharedPreferences preferences) {
		savedAccessToken = preferences.getString(accessTokenPreferenceName, null);
		if (savedAccessToken == null) {
			return null;
		}
		return savedAccessToken;
	}
	
	public String authenticateUrl(String requestUrl) {
		if (savedAccessToken == null) {
			return requestUrl;
		} else {
			return requestUrl + "?oauth_token=" + savedAccessToken;
		}
	}
}
