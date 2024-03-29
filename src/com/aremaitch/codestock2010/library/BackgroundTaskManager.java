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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import com.aremaitch.utils.ACLogger;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */

//  This class manages starting and stopping background tasks (services)
//  for scanning Twitter and cleaning up the databases.

public class BackgroundTaskManager {
    private static final long MS_IN_ONE_MINUTE = 60000;

    //  cleanup is every 24 hours at 11:59 PM
    private static final long DB_CLEANUP_INTERVAL = 86400000;     // 24 hours
    private static final int DB_CLEANUP_HOUR_OF_DAY = 23;
    private static final int DB_CLEANUP_MINUTE = 59;

    private static final int TWEET_SCAN_REQUEST_CODE = 1024;
    private static final int DB_CLEANUP_REQUEST_CODE = 1025;

    private Context _ctx;

    public BackgroundTaskManager(Context _ctx) {
        ACLogger.info(CSConstants.LOG_TAG, "BackgroundTaskmanager ctor");
        this._ctx = _ctx;
    }

    private PendingIntent createRecurringTweetScanPendingIntent() {
        return PendingIntent.getService(_ctx,
                TWEET_SCAN_REQUEST_CODE,
                new Intent(_ctx, TwitterTrackSvc.class).putExtra(TwitterConstants.TWEET_SCAN_HASHTAG_EXTRA_KEY, TwitterConstants.SEARCH_HASHTAGS),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createDBCleanupPendingIntent() {
        return PendingIntent.getService(_ctx,
                DB_CLEANUP_REQUEST_CODE,
                new Intent(_ctx, DatabaseCleanupSvc.class).putExtra(TwitterConstants.DB_CLEANUP_EXTRA_KEY, TwitterConstants.DB_CLEANUP_EXTRA_KEY),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Calendar getStartingDatabaseCleanupCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, DB_CLEANUP_HOUR_OF_DAY);
        cal.set(Calendar.MINUTE, DB_CLEANUP_MINUTE);
        return cal;
    }


    public void setDBCleanupTask() {
        ACLogger.info(CSConstants.LOG_TAG, "setting alarm for db cleanup task");

        getAlarmManager().setInexactRepeating(AlarmManager.RTC_WAKEUP,
                getStartingDatabaseCleanupCalendar().getTimeInMillis(),
                DB_CLEANUP_INTERVAL,
                createDBCleanupPendingIntent());
    }

    public void cancelDBCleanupTask() {
        ACLogger.info(CSConstants.LOG_TAG, "canceling alarm for db cleanup task");
        getAlarmManager().cancel(createDBCleanupPendingIntent());
    }


    public void setRecurringTweetScan() {
        ACLogger.info(CSConstants.LOG_TAG, "setting alarm for tweet scan");


        int updateMinutes = new CSPreferenceManager(_ctx).getTwitterUpdateInterval();

        getAlarmManager().setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + (MS_IN_ONE_MINUTE * updateMinutes),
                MS_IN_ONE_MINUTE * updateMinutes,
                createRecurringTweetScanPendingIntent());

        //  It's recommended not to use setRepeating() but to use set() and reset it upon each invocation.
//        getAlarmManager().set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + (MS_IN_ONE_MINUTE * updateMinutes), createRecurringTweetScanPendingIntent());
    }

    public void cancelRecurringTweetScan() {
        ACLogger.info(CSConstants.LOG_TAG, "canceling alarm for tweet scan");

        getAlarmManager().cancel(createRecurringTweetScanPendingIntent());
    }

    public void cancelAllRecurringTasks() {
        cancelRecurringTweetScan();
        cancelDBCleanupTask();
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager)_ctx.getSystemService(Context.ALARM_SERVICE);
    }
}
