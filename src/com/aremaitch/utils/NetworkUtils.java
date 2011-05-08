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

package com.aremaitch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    /**
     * Returns true if we have network connectivity.
     *
     * @param ctx A {@link Context}
     * @return True if the we have network connectivity, false otherwise.
     */
    public boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Returns true if the CodeStock website is reachable and online.
     *
     * @param ctx A {@link Context}
     * @return True if the website is reachable and online, false otherwise.
     */
    public boolean isCodeStockReachable(Context ctx) {
        try {
            URL url = new URL("http://codestock.org");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestProperty("User-Agent", "CodeStock Android App:" + new VersionUtils(ctx).getApplicationVersion());
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 15);  // only give 15 seconds to timeout
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns true if he user has permitted background data tasks.
     *
     * @param ctx A {@link Context}
     * @return True if background data transfers are permitted, false otherwise.
     */
    public boolean isBackgroundDataAllowed(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getBackgroundDataSetting();
    }
}
