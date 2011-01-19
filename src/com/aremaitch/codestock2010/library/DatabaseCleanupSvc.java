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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.aremaitch.codestock2010.repository.DatabaseCleanup;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseCleanupSvc extends Service {
    private static final String SERVICETAG = "CodeStockDBCleanupSvc";
    //  Not bindable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(SERVICETAG, "received onStartCommand");
        new DatabaseCleanupTask().execute();
        return Service.START_STICKY;
    }


    private class DatabaseCleanupTask extends AsyncTask<Void, Void, Void> {
        private PowerManager.WakeLock wl;

        @Override
        protected void onPreExecute() {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DatabaseCleanupTask");
            wl.acquire();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            wl.release();
            Log.i(SERVICETAG, "stopping service");
            DatabaseCleanupSvc.this.stopSelf();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseCleanup dbCleanUp = new DatabaseCleanup(DatabaseCleanupSvc.this);
            dbCleanUp.run();
            return null;
        }
    }
}
