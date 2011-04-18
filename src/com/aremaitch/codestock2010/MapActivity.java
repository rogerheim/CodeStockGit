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

package com.aremaitch.codestock2010;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import com.aremaitch.codestock2010.library.CSPreferenceManager;

public class MapActivity extends Activity {

    FlingListener flingListener;

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

        //  Create fling listener and attach to the horizontal scroll viewer that contains the map.
        flingListener = new FlingListener(this);
        HorizontalScrollView horizontalScrollView = (HorizontalScrollView)findViewById(R.id.map_horizontalscrollview);
        horizontalScrollView.setOnTouchListener(flingListener);

		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.map_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText("");
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return flingListener.get_detector().onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        //  Override back handler to go back to start
        SessionTracksActivity.startMe(this);
    }

    private class FlingListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
        Context ctx;
        GestureDetector detector;
        int horizontalMovementThreshold;
        int velocityThreshold;

        FlingListener(Context ctx) {
            this(ctx, null);
        }

        public FlingListener(Context ctx, GestureDetector detector) {
            if (detector == null) {
                detector = new GestureDetector(ctx, this);
            }
            this.ctx = ctx;
            this.detector = detector;
            CSPreferenceManager preferenceManager = new CSPreferenceManager(ctx);
            velocityThreshold = preferenceManager.getFlingVelocityThreshold();
            horizontalMovementThreshold = getWindowManager().getDefaultDisplay().getWidth() / preferenceManager.getFlingDistanceSensitivity();
        }

        @Override
        public boolean onFling(MotionEvent startEvent, MotionEvent endEvent, float velocityX, float velocityY) {
            if (Math.abs(velocityX) >= velocityThreshold) {
                //  Fling left; go back to home
                if (startEvent.getX() > endEvent.getX() && startEvent.getX() - endEvent.getX() > horizontalMovementThreshold) {
                    StartActivity.startMe(ctx);
                } else if (startEvent.getX() < endEvent.getX() && endEvent.getX() - startEvent.getX() > horizontalMovementThreshold) {
                    //  Fling right; finish and go back to sessions list
                    SessionTracksActivity.startMe(ctx);
                    overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
                }
            }
            return true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return detector.onTouchEvent(motionEvent);
        }

        public GestureDetector get_detector() {
            return detector;
        }
    }
}
