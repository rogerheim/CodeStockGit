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
import twitter4j.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "destroying service");
        _wl.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "received onStartCommand");

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        _wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TweetSearchTask");
        _wl.acquire();

        //  If the global background data setting is turned off, respect it and do not scan for tweets.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        _startedByAlarm = true;
        getHashTweetsSince(getLastMaxTweetId(), intent.getStringArrayExtra(TwitterConstants.TWEET_SCAN_HASHTAG_EXTRA_KEY));
        return Service.START_NOT_STICKY;
    }

    private TwitterStatusListener statusListener = null;
    public void startMonitoringStream(String[] hashTags, int[] userIds) {
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
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences prefs = getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(TwitterConstants.LAST_TWEETID_PREF, -1);
    }

    private void updateLastTweetId(long lastTweetId) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences prefs = getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putLong(TwitterConstants.LAST_TWEETID_PREF, lastTweetId);
        ed.commit();
    }

    private void getTwitterAccessToken() {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences prefs = getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        this._consumerKey = this.getString(R.string.twitter_oauth_key);
        this._consumerSecret = this.getString(R.string.twitter_oauth_secret);
        this._accessToken = prefs.getString(TwitterConstants.ACCESS_TOKEN_PREF, "");
        this._accessTokenSecret = prefs.getString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, "");
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
        public void onScrubGeo(int i, long l) {
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

            if (result != null) {
                for (Tweet tweet : result.getTweets()) {
                    _sinceId = Math.max(_sinceId, tweet.getId());
                    _dh.insertTweet(TweetObj.createInstance(tweet));
                    _tam.downloadAvatar(tweet.getFromUser(), tweet.getFromUserId(), tweet.getProfileImageUrl());
                    cntr++;
                }
                ACLogger.info(CSConstants.TWITTERTRACKSVC_LOG_TAG, "received " + String.valueOf(cntr) + " tweet(s)");
            }
            return null;
        }

        
        @Override
        protected void onPostExecute(Void aVoid) {
            _dh.close();
            updateLastTweetId(_sinceId);
            stopSelf();
//            _wl.release();
//            if (TwitterTrackSvc.this._startedByAlarm) {
//                TwitterTrackSvc.this.stopSelf();
//            }
        }
    }
}
