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

import android.text.TextUtils;
import com.aremaitch.utils.ACLogger;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterLib {

    private Twitter t = null;
    private TwitterStream tStream = null;

    public TwitterLib(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey)
            .setDebugEnabled(true)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessTokenSecret);

        Configuration config = cb.build();
        t = new TwitterFactory(config).getInstance();

    }

    public void startMonitoringStream(String[] hashTags, int[] userIds, StatusListener statusListener, ConnectionLifeCycleListener connectionLifeCycleListener)
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
        return t.search(q);
    }

    public int getUserIDFromScreenName(String screenName) throws TwitterException {
        int result = 0;
        ResponseList<User> users = t.lookupUsers(new String[] {screenName});
        if (users.size() > 0) {
            result = users.get(0).getId();
        }
        return result;
    }

    public HashMap<String, Integer> getUserIDsFromScreenNames(String[] screenNames) throws TwitterException {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        ResponseList<User> users = t.lookupUsers(screenNames);
        if (users.size() > 0) {
            for (User u : users) {
                result.put(u.getScreenName(), u.getId());
            }
        }
        return result;
    }

}
