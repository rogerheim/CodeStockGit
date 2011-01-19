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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */

/*
    If the user is permitting background updates when we receive the boot complete signal
    restart the services.
 */
public class CSBroadcastReceiver extends BroadcastReceiver {
    Context _ctx;
    Intent _intent;

    public void onReceive(Context context, Intent intent) {
        _ctx = context;
        _intent = intent;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            handleBootCompletedEvent();
        }

        //  Do we need to listen for other events that involve the radios? If the radios
        //  are turned off should we stop the background tasks?
    }


    private void handleBootCompletedEvent() {
        boolean runServiceInBackground = PreferenceManager.getDefaultSharedPreferences(_ctx)
                .getBoolean(TwitterConstants.TWITTER_BK_UPD_ENABLED_PREF, false);
        if (runServiceInBackground) {
            BackgroundTaskManager tm = new BackgroundTaskManager(_ctx);
            tm.setRecurringTweetScan();
            tm.setDBCleanupTask();
        }
    }
}
