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
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.Toast;
import com.aremaitch.codestock2010.library.AnalyticsManager;
import com.aremaitch.codestock2010.library.TwitterLib;

public class SessionDMTweetActivity extends Activity {
    //  Not clear_top; not for result.
    public static void startMe(Context ctx, String room, long sessionid) {
        ctx.startActivity(new Intent(ctx, SessionDMTweetActivity.class).putExtra("room", room).putExtra("sessionid", sessionid));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_dm_tweet_layout);

        int heightSet = 0;

        //TODO: must check this on different resolutions
        //  Looks ok on hdpi devices.
        if (getWindowManager().getDefaultDisplay().getOrientation() != Surface.ROTATION_0) {
            //  We are in landscape
            heightSet = LayoutParams.FILL_PARENT;
        } else {
            //  This results in too short of a window in landscape.
            heightSet = Math.round(getWindowManager().getDefaultDisplay().getHeight() / 1.5f);
        }
        getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT, heightSet);

        Intent intent = getIntent();
        final String room = intent.getStringExtra("room");
        final long sessionid = intent.getLongExtra("sessionid", 0);
        final RatingBar ratingBar = (RatingBar) findViewById(R.id.sessiondm_rating);
        // No bitly required here

        ((Button)findViewById(R.id.sessiondm_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TwitterLib tl = new TwitterLib(SessionDMTweetActivity.this);
                String dmString = String.format("rm:cs-%s sid:%d rating:%s", room, sessionid, ratingBar.getRating());
                boolean success = tl.sendRatingDM(dmString);
                AnalyticsManager.logFeedbackDM(SessionDMTweetActivity.this, success, sessionid, ratingBar.getRating());
                if (!success) {

                    //  As per Twitter terms I'm not permitted to automatically follow CodeStock; the user has to do it.
                    Toast.makeText(SessionDMTweetActivity.this,
                            getString(R.string.sessiondm_unable_to_send),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(SessionDMTweetActivity.this,
                            getString(R.string.sessiondm_success),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                finish();
            }
        });

        ((Button)findViewById(R.id.sessiondm_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}