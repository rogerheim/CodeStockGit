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

package com.aremaitch.codestock2010.datadownloader;

import com.aremaitch.codestock2010.library.IAgendaDownloader;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * Date: 2/10/11
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */

//  This is meant to be used by a background thread
public class ConferenceAgendaDownloader implements IAgendaDownloader {

    private final String SPEAKERS_URL = "http://codestock.org/api/v2.0.svc/AllSpeakersJson";
    private final String SESSIONS_URL = "http://codestock.org/api/v2.0.svc/AllSessionsJson";

    @Override
    public JSONObject getSpeakerData() {
        JSONObject result = null;

        try {
            DefaultHttpClient hc = new DefaultHttpClient();
            HttpGet hg = new HttpGet(getSpeakersUrl());
            HttpResponse response = hc.execute(hg);
            result = new JSONObject(EntityUtils.toString(response.getEntity()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public JSONObject getSessionData() {
        JSONObject result = null;

        try {
            DefaultHttpClient hc = new DefaultHttpClient();
            HttpGet hg = new HttpGet(getSessionUrl());
            HttpResponse response = hc.execute(hg);
            result = new JSONObject(EntityUtils.toString(response.getEntity()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getSpeakersUrl() {
        return SPEAKERS_URL;
    }

    private String getSessionUrl() {
        return SESSIONS_URL;
    }
}
