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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import com.aremaitch.codestock2010.library.AnalyticsManager;
import com.aremaitch.codestock2010.library.BitlyManager;
import com.aremaitch.codestock2010.library.TwitterLib;

public class SessionTweetActivity extends Activity {

    public static final int SESSION_TWEET_IAMHERE = 0;
    public static final int SESSION_TWEET_IAMGOING = 1;

    private static final String WHICH_EXTRA = "which";
    private static final String SESSIONURL_EXTRA = "sessionurl";
    private static final String SESSIONID_EXTRA = "sessionid";

    //  Not clear_top; not for result.
    public static void startMe(Context ctx, int which, String sessionUrl, long sessionid) {
        ctx.startActivity(new Intent(ctx, SessionTweetActivity.class)
                .putExtra(WHICH_EXTRA, which)
                .putExtra(SESSIONURL_EXTRA, sessionUrl)
                .putExtra(SESSIONID_EXTRA, sessionid));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_tweet_layout);
        setActivityLayout();


        Intent intent = getIntent();
        final int which = intent.getIntExtra(WHICH_EXTRA, SESSION_TWEET_IAMHERE);
        final String sessionUrl = intent.getStringExtra(SESSIONURL_EXTRA);
        final SessionTweetEditText tweet = (SessionTweetEditText) findViewById(R.id.sessiontweet_text);
        final long sessionid = intent.getLongExtra(SESSIONID_EXTRA, 0);
        final TextView charCount = (TextView)findViewById(R.id.sessiontweet_charcount);

        BitlyManager bitlyManager = null;
        BitlyManager.BitlyRunnable runnable = null;

        runnable = buildBitlyRunnable(which, tweet);

        switch (which) {
            case SESSION_TWEET_IAMHERE:
                ((TextView)findViewById(R.id.sessiontweet_instructions)).setText(getString(R.string.sessiontweet_imhere_instructions));
                bitlyManager = new BitlyManager(this, runnable);
                bitlyManager.shortenUrl(sessionUrl);
                break;

            case SESSION_TWEET_IAMGOING:
                ((TextView)findViewById(R.id.sessiontweet_instructions)).setText(getString(R.string.sessiontweet_imgoing_instructions));
                bitlyManager = new BitlyManager(this, runnable);
                bitlyManager.shortenUrl(sessionUrl);
                break;

            default:
                break;
        }

        tweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        tweet.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE && tweet.getText().length() > 0) {
                    sendTweet(tweet.getText().toString(), which, sessionid);
                    return true;
                }
                return false;
            }
        });

        ((Button)findViewById(R.id.sessiontweet_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                EditText tweet = (EditText) findViewById(R.id.sessiontweet_text);
                if (tweet.getText().length() > 0) {
                    String tweetText = tweet.getText().toString();
                    sendTweet(tweetText, which, sessionid);
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

    private void sendTweet(String tweetText, int which, long sessionid) {
        TwitterLib tl = new TwitterLib(this);
        boolean success = tl.sendTweet(tweetText);
        switch (which) {
            case SESSION_TWEET_IAMHERE:
                AnalyticsManager.logImHereTweet(this, success, sessionid);
                break;
            case SESSION_TWEET_IAMGOING:
                AnalyticsManager.logImGoingTweet(this, success, sessionid);
                break;
        }
        if (!success) {
            Toast.makeText(this,
                    getString(R.string.sessiontweet_unable_to_send),
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this,
                    getString(R.string.sessiontweet_success),
                    Toast.LENGTH_SHORT)
                    .show();
        }

        finish();
    }

    private BitlyManager.BitlyRunnable buildBitlyRunnable(final int which, final SessionTweetEditText tweet) {
        BitlyManager.BitlyRunnable runnable;
        runnable = new BitlyManager.BitlyRunnable() {
            @Override
            public void run() {
                String shortenedUrl = this.getShortenedUrl();
                if (TextUtils.isEmpty(shortenedUrl)) {
                    Toast.makeText(SessionTweetActivity.this, getString(R.string.sessiontweet_could_not_shorten), Toast.LENGTH_SHORT).show();
                } else {
                    ((ProgressBar)findViewById(R.id.sessiontweet_bitly_progress)).setVisibility(View.GONE);
                    tweet.setVisibility(View.VISIBLE);
                    switch (which) {
                        case SESSION_TWEET_IAMHERE:
                            tweet.setText("I'm at session " + shortenedUrl + " at #codestock");
                            break;
                        case SESSION_TWEET_IAMGOING:
                            tweet.setText("I'm going to session " + shortenedUrl + " at #codestock");
                            break;
                        default:
                            break;
                    }
                }
            }
        };
        return runnable;
    }

    private void setActivityLayout() {
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
        getWindow().setLayout(LayoutParams.FILL_PARENT, heightSet);
    }


}