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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;

/**
 * Interfoce to codestock.org ScheduleBuilder
 * @author roger
 *
 */
public class ScheduleBuilder {
	long _builderID = 0;
	Context context = null;
	String scheduleBuilderURL = "";
	String scheduleBuilderSvc = "";
	
	public ScheduleBuilder(Context currentContext, String scheduleBuilderURL, String scheduleBuilderSvc, long builderID) {
		context = currentContext;
		this.scheduleBuilderURL = scheduleBuilderURL;
		this.scheduleBuilderSvc = scheduleBuilderSvc;
		
		_builderID = builderID;
	}
	
	public ArrayList<Long> getBuiltSchedule() {
		ArrayList<Long> result = new ArrayList<Long>();
		
		JsonParser jp = null;
		JsonFactory f = new JsonFactory();
		
		try {
			URL theUrl = new URL(scheduleBuilderURL + "?" + scheduleBuilderSvc + "=" + Long.toString(this._builderID));
			
			jp = f.createJsonParser(theUrl);
			
			jp.nextToken();	// start object
			jp.nextToken(); //	Microsoft 'd'
			
			if (!jp.getCurrentName().equalsIgnoreCase("d")) {
				throw new JsonParseException("Error parsing schedule builder; first element is not 'd'", jp.getCurrentLocation());
			}
			
			jp.nextToken();	// start array
			
			while (jp.nextToken() != JsonToken.END_ARRAY) {
				result.add(jp.getLongValue());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (jp != null) {
					jp.close();
				}
			} catch (IOException e) {
			}
		}
		
		return result;
	}
}
