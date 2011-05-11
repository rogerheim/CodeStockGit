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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interfoce to codestock.org ScheduleBuilder
 * @author roger
 *
 */
public class ScheduleBuilder {
	long _builderID = 0;
	String scheduleBuilderURL = "http://codestock.org/api/v2.0.svc/GetScheduleJson";
	String scheduleBuilderSvc = "ScheduleID";
	String userIDFromEmailURL = "http://codestock.org/api/v2.0.svc/GetUserIDJson";
    String userIDFromEmailSvc = "Email";

	public ScheduleBuilder(long builderID) {

		_builderID = builderID;
	}

    public long getUserIDFromEmail(String emailAddress) {
        long userid = 0;

        DefaultHttpClient hc = new DefaultHttpClient();
        HttpGet hg = new HttpGet(userIDFromEmailURL + "?" + userIDFromEmailSvc + "=" + emailAddress);
        HttpResponse response;
        try {
            response = hc.execute(hg);
            String queryResult = EntityUtils.toString(response.getEntity());
            JSONObject jObj = new JSONObject(queryResult);

            userid = jObj.getLong("d");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userid;
    }

	public ArrayList<Long> getBuiltSchedule() {
		ArrayList<Long> result = new ArrayList<Long>();
		
		DefaultHttpClient hc = new DefaultHttpClient();
		HttpGet hg = new HttpGet(scheduleBuilderURL + "?" + scheduleBuilderSvc + "=" + Long.toString(this._builderID));
		HttpResponse response;
		try {
			response = hc.execute(hg);
			String queryResult = EntityUtils.toString(response.getEntity());
			JSONObject jObj;
			
			jObj = new JSONObject(queryResult);
			JSONArray dArray = jObj.getJSONArray("d");
			for (int i = 0; i <= dArray.length() - 1; i++) {
				result.add(dArray.getLong(i));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}
