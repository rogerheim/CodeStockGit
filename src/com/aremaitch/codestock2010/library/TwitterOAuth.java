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

package com.aremaitch.codestock2010.library;

import android.content.SharedPreferences;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterOAuth {
    //  Doesn't use TwitterLib because that class is for when we are already
    //  authenticated.

    Twitter t;
    AccessToken accessToken = null;
    RequestToken requestToken = null;
    User twitterUser = null;

    public TwitterOAuth() {
        t = new TwitterFactory().getInstance();
    }

    public boolean authenticateToTwitter(String consumerKey, String consumerSecret, String callbackUrl) throws TwitterException {
        t.setOAuthConsumer(consumerKey, consumerSecret);
        requestToken = t.getOAuthRequestToken(callbackUrl);
        return (requestToken != null);
    }

    public String getAuthorizationURL() throws IllegalStateException {
        if (requestToken == null)
            throw new IllegalStateException("Could not authenticate to Twitter; invalid RequestToken");
        return requestToken.getAuthorizationURL();
    }

    public String getAccessToken() {
        if (accessToken == null)
            getOAuthAccessToken();
        return accessToken.getToken();
    }

    public String getTokenSecret() {
        if (accessToken == null)
            getOAuthAccessToken();
        return accessToken.getTokenSecret();
    }

    public String getTwitterUserScreenName() {
        if (twitterUser == null)
            getTwitterUser();
        return twitterUser.getScreenName();
    }

    public long getTwitterUserID() {
        if (twitterUser == null)
            getTwitterUser();
        return twitterUser.getId();
    }

    public void saveOAuthTokens(SharedPreferences prefs, String accessToken, String tokenSecret, String screenName) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(TwitterConstants.ACCESS_TOKEN_PREF, accessToken);
        ed.putString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, tokenSecret);
        ed.putString(TwitterConstants.ACCESS_TOKEN_SCREENNAME_PREF, screenName);
        ed.commit();
    }

    public void forgetOAuthTokens(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TwitterConstants.ACCESS_TOKEN_PREF)
            .remove(TwitterConstants.ACCESS_TOKEN_SECRET_PREF)
            .remove(TwitterConstants.ACCESS_TOKEN_SCREENNAME_PREF)
            .commit();
    }


    private void getOAuthAccessToken() {
        try {
            accessToken = t.getOAuthAccessToken();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private void getTwitterUser() {
        try {
            twitterUser = t.verifyCredentials();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }


}
