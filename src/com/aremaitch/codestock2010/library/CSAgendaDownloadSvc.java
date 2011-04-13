
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

package com.aremaitch.codestock2010.library;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import com.aremaitch.codestock2010.R;
import com.aremaitch.codestock2010.StartActivity;
import com.aremaitch.codestock2010.datadownloader.ConferenceAgendaDownloader;
import com.aremaitch.codestock2010.repository.*;
import com.aremaitch.utils.ACLogger;

import java.beans.IndexedPropertyChangeEvent;

//  This handles one request at a time on a background thread.
//  It cannot handle multiple async requests (must complete a request
//  before starting the next one.) There normally won't be multiple
//  requests queued up, but it won't hurt anything if there are.
//  It doesn't need to continue running after completing its work
//  and doesn't need to continue running if the app is not in the
//  foreground. Sounds like a candidate for IntentService and letting
//  Android manage the lifetime.

public class CSAgendaDownloadSvc extends IntentService {


    public CSAgendaDownloadSvc() {
        super("CSAgendaDownloadSvc");
    }

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, CSAgendaDownloadSvc.class);
        ctx.startService(i);
        
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        // NB: This ctor for Notification is deprecated as of Honeycomb.
        Notification notification = new Notification(R.drawable.yodaskull_2011_icon,
                getString(R.string.refresh_data_progress_dialog_msg),
                System.currentTimeMillis());
        
        Intent notificationIntent = new Intent(this, StartActivity.class);

        //  If the task is already running (which it will be) then this flag simply brings it
        //  to the front without starting a new instance.
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //  This is the intent that will fire if the user clicks on the notification.
        //  (Isn't it interesting how we use the term "clicks" on a platform that has no mouse?)
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this,
                getString(R.string.refresh_data_progress_dialog_title),
                getString(R.string.refresh_data_progress_dialog_msg),
                pendingIntent);
        startForeground(R.string.refresh_data_progress_dialog_msg, notification);

        synchronized (this) {
            ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "starting agenda download");
            AgendaParser parser = new AgendaParser(new ConferenceAgendaDownloader());
            parser.doGetData();

            DataHelper dh = new DataHelper(this);
            dh.clearAllData();

            try {
                ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "saving tracks");
                for (Track t : parser.getParsedTracks()) {
                    dh.insertTrack(t);
                }
                ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "saving experience levels");
                for (ExperienceLevel l : parser.getParsedLevels()) {
                    dh.insertXPLevel(l);
                }
                ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "saving speakers");
                for (Speaker s : parser.getParsedSpeakers()) {
                    dh.insertSpeaker(s);
                }
                ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "saving sessions");
                for (Session s : parser.getParsedSessions()) {
                    dh.insertSession(s);
                }
            } finally {
                dh.close();
            }

            notification.tickerText = "Agenda download complete";

            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(R.string.refresh_data_progress_dialog_msg, notification);


            stopForeground(true);
            ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "agenda download complete");
        }
    }


}
