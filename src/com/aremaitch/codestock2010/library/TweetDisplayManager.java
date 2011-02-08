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
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import com.aremaitch.codestock2010.R;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.TweetObj;
import com.aremaitch.utils.ACLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/23/11
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class TweetDisplayManager {

    private final Context _ctx;

    private final Animation _fadeOutAnimation;
    private final Animation _fadeInAnimation;
    private final SimpleDateFormat _dateFormatter;
    private final View _tweetView0;
    private final View _tweetView1;
    private int _lastDisplayedView = -1;
    private long _tweetDisplayInterval;

    private final Handler displayTweetHandler = new Handler();
    private Timer tmr;
    private long _lastDisplayedTweetID;
    private final Drawable _defaultUserImage;
    private final TwitterAvatarManager _tam;

    public TweetDisplayManager(Context ctx, Animation fadeInAnimation, Animation fadeOutAnimation,
                               View tweetView0, View tweetView1, int tweetDisplaySeconds) {

        ACLogger.info(CSConstants.LOG_TAG, "TweetDisplayManager.ctor");
        _ctx = ctx;
        _fadeInAnimation = fadeInAnimation;
        _fadeOutAnimation = fadeOutAnimation;
        _tweetView0 = tweetView0;
        _tweetView1 = tweetView1;
        _tweetDisplayInterval = tweetDisplaySeconds * 1000;
        _dateFormatter = new SimpleDateFormat("MMM d yyyy h:mm a");
        _defaultUserImage = new BitmapDrawable(_ctx.getResources(), BitmapFactory.decodeResource(_ctx.getResources(), R.drawable.ic_contact_picture));
        _tam = new TwitterAvatarManager(ctx);
    }


    public void startTweetDisplayTimer() {
        ACLogger.info(CSConstants.LOG_TAG, "starting tweet display timer");

        _lastDisplayedTweetID = _ctx.getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .getLong(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF, 0);

        tmr = new Timer();
        tmr.scheduleAtFixedRate(createTimerTask(), new Date(), _tweetDisplayInterval);
    }

    public void stopTweetDisplayTimer() {
        ACLogger.info(CSConstants.LOG_TAG, "stopping tweet display timer");
        if (tmr != null) {
            tmr.cancel();
            saveLastDisplayedTweetID(_lastDisplayedTweetID);
        }
    }

    private Runnable tweetDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            TweetObj tt = getNextTweet();
            if (tt != null) {
                if (_lastDisplayedView == 0) {
//                    _tweetView1 = updateTweetView(tt, _tweetView1);
                    updateTweetView(tt, _tweetView1);
                    _tweetView0.startAnimation(_fadeOutAnimation);
                    _tweetView0.setVisibility(View.GONE);
                    _tweetView1.startAnimation(_fadeInAnimation);
                    _tweetView1.setVisibility(View.VISIBLE);
                    _lastDisplayedView = 1;
                } else {
//                    _tweetView0 = updateTweetView(tt, _tweetView0);
                    updateTweetView(tt, _tweetView0);
                    _tweetView1.startAnimation(_fadeOutAnimation);
                    _tweetView1.setVisibility(View.GONE);
                    _tweetView0.startAnimation(_fadeInAnimation);
                    _tweetView0.setVisibility(View.VISIBLE);
                    _lastDisplayedView = 0;
                }
                _lastDisplayedTweetID = tt.getId();
            }
        }
    };

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                displayTweetHandler.post(tweetDisplayRunnable);
            }
        };
    }

    private void saveLastDisplayedTweetID(long lastDisplayedTweetID) {
        SharedPreferences.Editor editor = _ctx.getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF, lastDisplayedTweetID);
        editor.commit();


    }

    //  Changed name from inflateTweetView because we aren't actually 'inflating' anything.
    //  It's loaded via xml layout.
    private void updateTweetView(TweetObj tt, View view) {
        TweetViewHolder holder;
        if (view.getTag() == null) {
            holder = new TweetViewHolder();
            holder.avatar = (ImageView) view.findViewById(R.id.twitter_avatar);
            holder.createdAt = (TextView) view.findViewById(R.id.tweet_createdat);
            holder.screenName = (TextView) view.findViewById(R.id.twitter_screenname);
            holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
            view.setTag(holder);
        } else {
            holder = (TweetViewHolder)view.getTag();
        }
        holder.tweetID = tt.getId();
        holder.screenName.setText(tt.getFromUser());
        holder.tweetText.setText(tt.getText());
        holder.createdAt.setText(_dateFormatter.format(tt.getCreatedAt()));

        //  User avatar's are stored using their real id, not their search api id.

        holder.avatar.setImageDrawable(getUserAvatar(tt.getFromUser()));
//        return view;
    }

    private Drawable getUserAvatar(String screenName) {
        //  Get the sending user's avatar from cache. If not there, return the generic
        //  Android contact image.
        Drawable image = _tam.getTwitterAvatar(screenName);
        if (image == null) {
            return _defaultUserImage;
        }
        return image;
    }

    private TweetObj getNextTweet() {
        DataHelper dh = new DataHelper(_ctx);
        TweetObj tt = dh.getNextTweet(_lastDisplayedTweetID);
        dh.close();
        return tt;
    }

    static class TweetViewHolder {
        long tweetID;
        ImageView avatar;
        TextView screenName;
        TextView tweetText;
        TextView createdAt;
    }
}
