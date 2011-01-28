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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import com.aremaitch.codestock2010.R;
import com.aremaitch.utils.ACLogger;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

//	Manages countdown to CodeStock 2011

public class CountdownManager {

    private final long csStart = new GregorianCalendar(2011, GregorianCalendar.JUNE, 3, 8, 0, 0).getTimeInMillis();    // the magic day!
    private TextView tvdays;
    private TextView tvhours;
    private TextView tvminutes;
    private TextView tvseconds;
    private Timer tmr;

    private final long msIn1Minute = 1000 * 60;                    // milliseconds in 1 minute
    private final long msIn1Hour = msIn1Minute * 60;            // milliseconds in 1 hour
    private final long msIn1Day = msIn1Hour * 24;                // milliseconds in 1 day

    DecimalFormat df3digitsnolead = new DecimalFormat("##0");
    DecimalFormat df2digits = new DecimalFormat("00");
    DecimalFormat df2digitsnolead = new DecimalFormat("#0");

//    private final String format2Digits = "%02d";                // make all digit parts have leading zeros
//    private final String format3Digits = "%03d";

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                ACLogger.info(CSConstants.LOG_TAG, "CountdownManager.TimerTask.Runnable.run()");
                long diff = csStart - System.currentTimeMillis();
                //	Cause you know someone will set their phone's clock ahead
                //	just to see what happens...
                if (diff <= 0) {
                    updateCountdownDisplay(0, 0, 0, 0);
                    stop();
                }

                int iDays = Math.round(diff / msIn1Day);
                diff -= (iDays * msIn1Day);

                int iHours = Math.round(diff / msIn1Hour);
                diff -= (iHours * msIn1Hour);

                int iMinutes = Math.round(diff / msIn1Minute);
                diff -= (iMinutes * msIn1Minute);

                //	diff now contains the number of remaining seconds ( as milliseconds )
                int iSeconds = Math.round(diff / 1000);

                updateCountdownDisplay(iDays, iHours, iMinutes, iSeconds);

            }
        };



    void updateCountdownDisplay(int iDays, int iHours, int iMinutes, int iSeconds) {

        tvdays.setText(df3digitsnolead.format(iDays));                // no leading zeros; number can be 3, 2, or 1 digit
        tvhours.setText(df2digitsnolead.format(iHours));              // no leading zeros; number can be 2 or 1 digit
        tvminutes.setText(df2digitsnolead.format(iMinutes));          // no leading zeros; number can be 2 or 1 digit
        tvseconds.setText(df2digits.format(iSeconds));                // leading zeros; number will always be 2 digits

        //  There's terrible memory pressure when using String.format (GC 10 - 12K objects every 10 seconds.)
        //  By using DecimalFormat.format() the pressure is 15 - 16K every 8 minutes.
        //  Still not great, but better.
        //  See http://groups.google.com/group/android-developers/browse_thread/thread/288ed6909459538e#

//        tvdays.setText(String.format(format3Digits, iDays));
//        tvhours.setText(String.format(format2Digits, iHours));
//        tvminutes.setText(String.format(format2Digits, iMinutes));
//        tvseconds.setText(String.format(format2Digits, iSeconds));
    }

    public void initializeCountdown(View digitsContainer, AssetManager assets) {


        Typeface tf = Typeface.createFromAsset(assets, "fonts/LCD2N___.TTF");
        tvdays = (TextView) digitsContainer.findViewById(R.id.countdown_days);
        tvhours = (TextView) digitsContainer.findViewById(R.id.countdown_hours);
        tvminutes = (TextView) digitsContainer.findViewById(R.id.countdown_minutes);
        tvseconds = (TextView) digitsContainer.findViewById(R.id.countdown_seconds);

        tvdays.setTypeface(tf);
        tvhours.setTypeface(tf);
        tvminutes.setTypeface(tf);
        tvseconds.setTypeface(tf);
    }

    public void start() {
        // start now and call tTask.run() every second
        tmr = new Timer();
        ACLogger.info(CSConstants.LOG_TAG, "starting countdown timer");
        tmr.scheduleAtFixedRate(createNewTimerTask(), new Date(), 1000);
    }

    private TimerTask createNewTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
//                ACLogger.info(CSConstants.LOG_TAG, "CountdownManager.TimerTask.run()");
                handler.post(runnable);
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        ACLogger.info(CSConstants.LOG_TAG, "CountdownManager.TimerTask.Runnable.run()");
//                        long diff = csStart - scheduledExecutionTime();
//                        //	Cause you know someone will set their phone's clock ahead
//                        //	just to see what happens...
//                        if (diff <= 0) {
//                            updateCountdownDisplay(0,0,0,0);
//                            stop();
//                        }
//
//                        int iDays = Math.round(diff / msIn1Day);
//                        diff -= (iDays * msIn1Day);
//
//                        int iHours = Math.round(diff / msIn1Hour);
//                        diff -= (iHours * msIn1Hour);
//
//                        int iMinutes = Math.round(diff / msIn1Minute);
//                        diff -= (iMinutes * msIn1Minute);
//
//                        //	diff now contains the number of remaining seconds ( as milliseconds )
//                        int iSeconds = Math.round(diff / 1000);
//
//                        updateCountdownDisplay(iDays, iHours, iMinutes, iSeconds);
//
//                    }
//                });
            }
        };
    }

    public void stop() {
        //	so when we go to sleep, the clock stops
        ACLogger.info(CSConstants.LOG_TAG, "stopping countdown timer");

        //  Once the timer is canceled nothing can be rescheduled on it.
        tmr.cancel();
    }
}
