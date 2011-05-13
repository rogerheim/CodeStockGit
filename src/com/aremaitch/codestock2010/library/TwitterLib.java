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

import android.content.Context;
import android.text.TextUtils;
import com.aremaitch.codestock2010.R;
import com.aremaitch.utils.ACLogger;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */

// 28-Mar-2011  New twitter4j changed user id's from int to long.

public class TwitterLib {

    private Twitter t = null;
    private TwitterStream tStream = null;

    public TwitterLib(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        buildObject(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

    public TwitterLib(Context ctx) {
        // Use this method to have the library get the keys itself.
        String consumerKey = ctx.getString(R.string.twitter_oauth_key);
        String consumerSecret = ctx.getString(R.string.twitter_oauth_secret);
        CSPreferenceManager preferenceManager = new CSPreferenceManager(ctx);
        String accessToken = preferenceManager.getTwitterAccessToken();
        String accessTokenSecret = preferenceManager.getTwitterAccessTokenSecret();
        buildObject(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

    private void buildObject(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey)
            .setDebugEnabled(true)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessTokenSecret);

        Configuration config = cb.build();
        t = new TwitterFactory(config).getInstance();
    }

    public void startMonitoringStream(String[] hashTags, long[] userIds, StatusListener statusListener, ConnectionLifeCycleListener connectionLifeCycleListener)
        throws TwitterException {

        if (statusListener != null)
            tStream.addListener(statusListener);
        if (connectionLifeCycleListener != null)
            tStream.addConnectionLifeCycleListener(connectionLifeCycleListener);

        FilterQuery fq = new FilterQuery();
        fq.track(hashTags);
        if (userIds != null)
            fq.follow(userIds);

        tStream.filter(fq);
    }

    public void stopMonitoringStream() {
        if (tStream != null) {
            try {
                tStream.shutdown();
            } catch (IllegalStateException ex) {
                ACLogger.warn(CSConstants.LOG_TAG, "warning: shutting down stream that is already shutdown");
            }
            tStream = null;
        }
    }

    public QueryResult search(long sinceId, String[] hashTags) throws TwitterException {
        Query q = new Query();
        if (sinceId >= 0) {
            q.setSinceId(sinceId);
        }

        q.setQuery(TextUtils.join(" OR ", hashTags));
        try {
            return t.search(q);
        } catch (NumberFormatException nfe) {
            ACLogger.error(CSConstants.LOG_TAG, "number format exception was thrown by twitter4j");
        }
        return null;
    }

    public long getUserIDFromScreenName(String screenName) throws TwitterException {
        long result = 0;
        ResponseList<User> users = t.lookupUsers(new String[] {screenName});
        if (users.size() > 0) {
            result = users.get(0).getId();
        }
        return result;
    }

    public HashMap<String, Long> getUserIDsFromScreenNames(List<String> screenNames) throws TwitterException {
        HashMap<String, Long> result = new HashMap<String, Long>();

        //  Talk about awkward syntax:
        ResponseList<User> users = t.lookupUsers(screenNames.toArray(new String[screenNames.size()]));
        if (users.size() > 0) {
            for (User u : users) {
                result.put(u.getScreenName(), u.getId());
            }
        }
        return result;
    }

    public boolean sendTweet(String tweetText) {
        boolean result = false;
        try {
            t.updateStatus(tweetText);
            result = true;
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean sendRatingDM(String dmText) {
        boolean result = false;
        try {
            t.sendDirectMessage(TwitterConstants.CODESTOCK_USERID, dmText);
            result = true;
            ACLogger.info(CSConstants.LOG_TAG, "successfully sent dm rating: " + dmText);
        } catch (TwitterException e) {
            if (e.getStatusCode() == 403) {
                ACLogger.info(CSConstants.LOG_TAG, "could not dm rating: " + e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
        return result;
    }

}
