/*
   Copyright 2010 Roger Heim

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.aremaitch.codestock2010.datadownloader;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interfoce to codestock.org ScheduleBuilder
 * @author roger
 *
 */
public class ScheduleBuilder {
	long _builderID = 0;
	String scheduleBuilderURL = "";
	String scheduleBuilderSvc = "";
	
	public ScheduleBuilder(String scheduleBuilderURL, String scheduleBuilderSvc, long builderID) {
		this.scheduleBuilderURL = scheduleBuilderURL;
		this.scheduleBuilderSvc = scheduleBuilderSvc;
		
		_builderID = builderID;
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
