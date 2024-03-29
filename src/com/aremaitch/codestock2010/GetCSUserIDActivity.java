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

public class GetCSUserIDActivity extends Activity {
    public static final int RESULT_QRCODE = Activity.RESULT_FIRST_USER;
    public static final int RESULT_EMAIL = Activity.RESULT_FIRST_USER + 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getcsuserid_layout);



        ((Button)findViewById(R.id.getcsuserid_scanqr)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_QRCODE);
                finish();
            }
        });

        ((Button)findViewById(R.id.getcsuserid_email)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_EMAIL);
                finish();
            }
        });

        ((Button)findViewById(R.id.getcsuserid_skipit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

}