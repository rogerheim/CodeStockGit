/*
 * Copyright 2010-2011 Roger Heim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aremaitch.codestock2010;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import com.aremaitch.codestock2010.library.AnalyticsManager;
import com.aremaitch.codestock2010.library.BitlyManager;
import com.aremaitch.codestock2010.library.TwitterLib;

public class SessionTweetActivity extends Activity {

    public static final int SESSION_TWEET_IAMHERE = 0;
    public static final int SESSION_TWEET_IAMGOING = 1;

    //  Not clear_top; not for result.
    public static void startMe(Context ctx, int which, String sessionUrl, long sessionid) {
        ctx.startActivity(new Intent(ctx, SessionTweetActivity.class)
                .putExtra("which", which)
                .putExtra("sessionurl", sessionUrl)
                .putExtra("sessionid", sessionid));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_tweet_layout);

        //setLayout(width, height)
        // make this 1.5's the height of the screen
        int heightSet = 0;

        //TODO: must check this on different resolutions
        if (getWindowManager().getDefaultDisplay().getOrientation() != Surface.ROTATION_0) {
            //  We are in landscape
            heightSet = LayoutParams.FILL_PARENT;
        } else {
            //  This results in too short of a window in landscape.
            heightSet = Math.round(getWindowManager().getDefaultDisplay().getHeight() / 1.5f);
        }
        getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT, heightSet);

        Intent intent = getIntent();
        final int which = intent.getIntExtra("which", SESSION_TWEET_IAMHERE);
        final String sessionUrl = intent.getStringExtra("sessionurl");
        final EditText tweet = (EditText)findViewById(R.id.sessiontweet_text);
        final long sessionid = intent.getLongExtra("sessionid", 0);
        BitlyManager bitlyManager = null;
        BitlyManager.BitlyRunnable runnable = null;

        switch (which) {
            case SESSION_TWEET_IAMHERE:
                ((TextView)findViewById(R.id.sessiontweet_instructions)).setText(getString(R.string.sessiontweet_imhere_instructions));
                runnable = new BitlyManager.BitlyRunnable() {
                    @Override
                    public void run() {
                        //  Could this be possibly localized? Should it be in a string resource?
                        tweet.setText("I'm at session " + this.getShortenedUrl() + " at #codestock");
                    }
                };
                bitlyManager = new BitlyManager(this, runnable);
                bitlyManager.shortenUrl(sessionUrl);
                break;

            case SESSION_TWEET_IAMGOING:
                ((TextView)findViewById(R.id.sessiontweet_instructions)).setText(getString(R.string.sessiontweet_imgoing_instructions));

                runnable = new BitlyManager.BitlyRunnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(this.getShortenedUrl())) {
                            Toast.makeText(SessionTweetActivity.this, getString(R.string.sessiontweet_could_not_shorten),
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            tweet.setText("I'm going to session " + this.getShortenedUrl() + " at #codestock");
                        }
                    }
                };
                bitlyManager = new BitlyManager(this, runnable);
                bitlyManager.shortenUrl(sessionUrl);
                break;

            default:
                break;
        }

        ((Button)findViewById(R.id.sessiontweet_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tweet = (EditText)findViewById(R.id.sessiontweet_text);
                if (tweet.getText().length() > 0) {
                    String tweetText = tweet.getText().toString();
                    TwitterLib tl = new TwitterLib(SessionTweetActivity.this);
                    boolean success = tl.sendTweet(tweetText);
                    switch (which) {
                        case SESSION_TWEET_IAMHERE:
                            AnalyticsManager.logImHereTweet(SessionTweetActivity.this, success, sessionid);
                            break;
                        case SESSION_TWEET_IAMGOING:
                            AnalyticsManager.logImGoingTweet(SessionTweetActivity.this, success, sessionid);
                            break;
                    }
                    if (!success) {
                        Toast.makeText(SessionTweetActivity.this,
                                getString(R.string.sessiontweet_unable_to_send),
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(SessionTweetActivity.this,
                                getString(R.string.sessiontweet_success),
                                Toast.LENGTH_SHORT)
                                .show();
                    }

                    finish();
                } else {
                    Toast.makeText(SessionTweetActivity.this, "Enter something to tweet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((Button)findViewById(R.id.sessiontweet_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}