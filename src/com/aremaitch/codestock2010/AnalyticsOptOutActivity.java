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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import com.aremaitch.codestock2010.library.CSPreferenceManager;

/**
 * Created by IntelliJ IDEA.
 * User: roger
 * Date: 5/11/11
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsOptOutActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_optout_layout);

        final CheckBox optIn = (CheckBox)findViewById(R.id.analyticsoptout_check);
        final CSPreferenceManager prefMgr = new CSPreferenceManager(this);

        optIn.setChecked(prefMgr.isParticipatingInAnalytics());

        ((Button)findViewById(R.id.analyticsoptout_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (optIn.isChecked()) {
                    prefMgr.setParticipatingInAnalytics(true);
                } else {
                    prefMgr.setParticipatingInAnalytics(false);
                }
                finish();
            }
        });

        ((Button)findViewById(R.id.analyticsoptout_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}