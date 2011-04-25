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
import com.aremaitch.codestock2010.library.CountdownManager;
import com.aremaitch.codestock2010.library.QuickActionMenu;
import com.aremaitch.codestock2010.library.QuickActionMenuManager;

public class MapActivity extends Activity {

    CountdownManager cMgr;
    View digitsContainer;
    QuickActionMenuManager qaMgr;

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

        initializeCountdownClock();


		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.map_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText("");
	}

    @Override
    protected void onPause() {
        qaMgr.destroyQuickActionMenu();
        stopCountdownClock();
        super.onPause();
    }

    @Override
    protected void onResume() {
        qaMgr = new QuickActionMenuManager(findViewById(R.id.footer_logo));

        qaMgr.initializeQuickActionMenu();
        startCountdownClock();
        super.onResume();
    }


    private void initializeCountdownClock() {
        cMgr = new CountdownManager();
        digitsContainer = findViewById(R.id.countdown_digit_container);
    }

    private void startCountdownClock() {
        cMgr.initializeCountdown(digitsContainer, getAssets());
        cMgr.start();
    }

    private void stopCountdownClock() {
        cMgr.stop();
    }


}
