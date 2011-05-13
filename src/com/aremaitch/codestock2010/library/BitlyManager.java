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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import com.aremaitch.codestock2010.R;
import com.aremaitch.utils.NetworkUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BitlyManager {


    private BitlyRunnable runnable;
    private Context ctx;

    public BitlyManager(Context ctx, BitlyRunnable runnable) {
        this.ctx = ctx;
        this.runnable = runnable;
    }

    public void shortenUrl(String longUrl) {
        new ShortenUrlTask(ctx, longUrl, this.runnable).execute();
    }

    private class ShortenUrlTask extends AsyncTask<Void, Void, String> {

        String longUrl;

        Context ctx;
        BitlyRunnable runnable;
        private ShortenUrlTask(Context ctx, String longUrl, BitlyRunnable runnable) {
            this.ctx = ctx;
            this.longUrl = longUrl;
            this.runnable = runnable;
        }

        @Override
        protected void onPostExecute(String shortenedUrl) {
            Handler handler = new Handler();
            this.runnable.setShortenedUrl(shortenedUrl);
            handler.post(this.runnable);

        }

        @Override
        protected String doInBackground(Void... voids) {
            String shortenedUrl = "";
            JSONObject result = null;

            if (new NetworkUtils().isOnline(this.ctx)) {
                Uri.Builder builder = Uri.parse("http://api.bitly.com/v3/shorten").buildUpon();
                builder.appendQueryParameter("longUrl", this.longUrl);
                builder.appendQueryParameter("format", "json");
                builder.appendQueryParameter("login", ctx.getString(R.string.bitly_login));
                builder.appendQueryParameter("apiKey", ctx.getString(R.string.bitly_apikey));


                try {
                    URL bitlyURL = new URL(builder.build().toString());
                    DefaultHttpClient hc = new DefaultHttpClient();
                    HttpGet hg = new HttpGet(builder.build().toString());
                    HttpResponse response = hc.execute(hg);
                    result = new JSONObject(EntityUtils.toString(response.getEntity()));

                    if (result.getInt("status_code") == 200) {
                        JSONObject data = result.getJSONObject("data");
                        shortenedUrl = data.getString("url");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return shortenedUrl;
        }

    }

    public static class BitlyRunnable implements Runnable {

        private String shortenedUrl;

        @Override
        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getShortenedUrl() {
            return shortenedUrl;
        }

        public void setShortenedUrl(String shortenedUrl) {
            this.shortenedUrl = shortenedUrl;
        }
    }
}
