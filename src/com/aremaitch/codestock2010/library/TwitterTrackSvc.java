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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import com.aremaitch.codestock2010.R;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.TweetObj;
import com.aremaitch.utils.ACLogger;
import com.flurry.android.FlurryAgent;
import twitter4j.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */

// 28-Mar-2011  New twitter4j changed user id's from int to long.

public class TwitterTrackSvc extends Service {
    private String _consumerKey;
    private String _consumerSecret;
    private String _accessToken;
    private String _accessTokenSecret;
    private TwitterLib t;
    private boolean _startedByAlarm = false;
    private PowerManager.WakeLock _wl;

    public class TwitterTrackSvcBinder extends Binder {
        public TwitterTrackSvc getService() {
            return TwitterTrackSvc.this;
        }
    }

    private final IBinder svcBinder = new TwitterTrackSvcBinder();
    public IBinder onBind(Intent intent) {
        _startedByAlarm = false;
        return svcBinder;
    }

    @Override
    public void onCreate() {
        ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "service created");
        getTwitterAccessToken();
        t = new TwitterLib(this._consumerKey, this._consumerSecret, this._accessToken, this._accessTokenSecret);
        super.onCreate();
    }

    //TODO: change order to log message, release wakelock, call super class.
    @Override
    public void onDestroy() {
        super.onDestroy();
        ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "destroying service");
        _wl.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "received onStartCommand");

        FlurryAgent.logEvent(FlurryEvent.TWITTER_SVC_START);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        _wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TweetSearchTask");
        _wl.acquire();

        //  If the global background data setting is turned off, respect it and do not scan for tweets.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            FlurryAgent.logEvent(FlurryEvent.TWITTER_SVC_STOP_NO_BKGND);
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        _startedByAlarm = true;
        getHashTweetsSince(getLastMaxTweetId(), intent.getStringArrayExtra(TwitterConstants.TWEET_SCAN_HASHTAG_EXTRA_KEY));
        return Service.START_NOT_STICKY;
    }

    private TwitterStatusListener statusListener = null;
    public void startMonitoringStream(String[] hashTags, long[] userIds) {
        statusListener = new TwitterStatusListener();
        try {
            t.startMonitoringStream(hashTags, userIds, statusListener, null);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void stopMonitoringStream() {
        t.stopMonitoringStream();
        updateLastTweetId(statusListener.getHighestReceivedTweetId());
        statusListener = null;
    }

    private void getHashTweetsSince(long sinceId, String[] hashTags) {
        new TweetSearchTask(this, sinceId, hashTags).execute();
    }

    private long getLastMaxTweetId() {
        return new CSPreferenceManager(this).getLastRetrievedTweetId();
    }

    private void updateLastTweetId(long lastTweetId) {
        new CSPreferenceManager(this).setLastRetrievedTweetId(lastTweetId);
    }

    private void getTwitterAccessToken() {
        this._consumerKey = this.getString(R.string.twitter_oauth_key);
        this._consumerSecret = this.getString(R.string.twitter_oauth_secret);
        CSPreferenceManager preferenceManager = new CSPreferenceManager(this);
        this._accessToken = preferenceManager.getTwitterAccessToken();
        this._accessTokenSecret = preferenceManager.getTwitterAccessTokenSecret();
    }

    class TwitterStatusListener implements StatusListener {
        private DataHelper _dh;
        private long _lastTweetId = -1;

        public TwitterStatusListener() {
            _dh = new DataHelper(TwitterTrackSvc.this);
        }

        @Override
        public void onStatus(Status status) {
            _lastTweetId = Math.max(_lastTweetId, status.getId());
            TweetObj to = TweetObj.createInstance(status);
            _dh.insertTweet(to);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            _dh.deleteTweet(statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onScrubGeo(long l, long l1) {
            //To change body of implemented methods use File | Settings | File Templates.
        }


        @Override
        public void onException(Exception e) {
            ACLogger.error(CSConstants.TWITTERTRACKSVC_LOG_TAG, "Stream exception:");
            e.printStackTrace();
        }

        public long getHighestReceivedTweetId() {
            return this._lastTweetId;
        }
    }

    class TweetSearchTask extends AsyncTask<Void, Void, Void> {
        private long _sinceId;
        private String[] _hashTags;
        DataHelper _dh;
        TwitterAvatarManager _tam;
        Context _ctx;

        TweetSearchTask(Context ctx, long _sinceId, String[] _hashTags) {
            this._ctx = ctx;
            this._sinceId = _sinceId;
            this._hashTags = _hashTags;
        }

        @Override
        protected void onPreExecute() {
            _dh = new DataHelper(TwitterTrackSvc.this);
            _tam = new TwitterAvatarManager(_ctx);
        }

        @Override
        protected Void doInBackground(Void... params) {
            QueryResult result = null;
            int cntr = 0;
            try {
                result = t.search(_sinceId, _hashTags);
            } catch (TwitterException ex) {
                ACLogger.error(CSConstants.TWITTERTRACKSVC_LOG_TAG, "error performing twitter search: " + ex.getMessage());
//                ex.printStackTrace();
            }

            //  Need to check if result.getTweets() returns a zero length collection.
            if (result != null && result.getTweets().size() > 0) {
                try {
                    HashMap<String, Long> userIdMap = t.getUserIDsFromScreenNames(extractUserNames(result.getTweets()));
                    for (Tweet tweet : result.getTweets()) {
                        //  As per Twitter developer docs, the user id's from the search API are _not_ the real
                        //  user id's. The real user id's come from the other api. You need to call
                        //  a different api to get the real userid from the display name.

                        _dh.insertTweet(TweetObj.createInstance(tweet));

                        //  Note: user lookup may not return the same number of users as screen names we passed in.
                        //    If we pass in 5 user names and 1 of them is unknown, suspended, or deleted the result list
                        //    will only include the 4 valid ones.

                        //  Twitter screen names are case in-sensitive but hashmap keys are.
                        //  hashmap.get() is case-sensitive as well

                        long realUserId = getRealUserId(userIdMap, tweet.getFromUser());
                        if (realUserId > -1) {
                            _tam.downloadAvatar(tweet.getFromUser(), realUserId, tweet.getProfileImageUrl());
                        } else {
                            ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "no userid mapping for screenname \"" +
                                tweet.getFromUser() + "\"");

                        }
                        cntr++;
                    }
                } catch (TwitterException e) {
                    ACLogger.error(CSConstants.TWITTERTRACKSVC_LOG_TAG, "error retrieving twitter user ids: " + e.getMessage());
                }
                _sinceId = result.getMaxId();
            }
            ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "received " + String.valueOf(cntr) + " tweet(s)");
            return null;
        }

        private long getRealUserId(HashMap<String, Long> map, String screenName) {
            long result = -1;
            for (String s : map.keySet()) {
                if (s.equalsIgnoreCase(screenName)) {
                    result = map.get(s);
                    break;
                }
            }
            return result;
        }

        //  avoid duplicates
        private ArrayList<String> extractUserNames(List<Tweet> tweets) {
            ArrayList<String> tempList = new ArrayList<String>();
            for (int i = 0; i <= tweets.size() - 1; i++) {
                if (!tempList.contains(tweets.get(i).getFromUser())) {
                    tempList.add(tweets.get(i).getFromUser());
                }
            }
            ACLogger.info(CSConstants.LOG_TAG, "extractUserNames input size= " + tweets.size() +
                    " output list size=" + tempList.size());
            return tempList;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            _dh.close();
            updateLastTweetId(_sinceId);
            FlurryAgent.logEvent(FlurryEvent.TWITTER_SVC_STOP);
            stopSelf();
        }


    }
}
