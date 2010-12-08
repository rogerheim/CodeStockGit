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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;
import com.aremaitch.utils.ACLogger;

/**
 * Class for downloading data from CodeStock Version 2 API
 * @author Roger Heim, Aremaitch Consulting
 *
 */
public class DownloaderV2 {
	Context context = null;
	String roomsUrl = "";
	String speakersUrl = "";
	String sessionsUrl = "";
	
	List<Speaker> parsedSpeakers = new ArrayList<Speaker>();
	List<Track> parsedTracks = new ArrayList<Track>();
	List<Session> parsedSessions = new ArrayList<Session>();
	List<ExperienceLevel> parsedLevels = new ArrayList<ExperienceLevel>();
	
	public List<Speaker> getParsedSpeakers() {
		return parsedSpeakers;
	}
	
	public List<Track> getParsedTracks() {
		return parsedTracks;
	}
	
	public List<Session> getParsedSessions() {
		return parsedSessions;
	}
	
	public List<ExperienceLevel> getParsedLevels() {
		return parsedLevels;
	}
	
	public DownloaderV2(Context currentContext, String roomsUrl, String speakersUrl, String sessionsUrl) {
		this.context = currentContext;
		this.roomsUrl = roomsUrl;
		this.speakersUrl = speakersUrl;
		this.sessionsUrl = sessionsUrl;
	}
	
	public boolean getCodeStockData() {
		boolean result = true;
		
		//	not actually using room data
		try {
			getSpeakerData(this.speakersUrl);
			getSessionData(this.sessionsUrl);
		} catch (Exception ex) {
			result = false;
		}
		
		return result;
	}
	
	private void getSpeakerData(String speakersUrl) throws ClientProtocolException, IOException, JSONException {
		ACLogger.info("CodeStock Downloader", "getSpeakerData from " + speakersUrl);
		
		DefaultHttpClient hc = new DefaultHttpClient();
		HttpGet hg = new HttpGet(speakersUrl);
		
		HttpResponse response = hc.execute(hg);
		String queryResult = EntityUtils.toString(response.getEntity());
		JSONObject jObj;
			
		jObj = new JSONObject(queryResult);
		JSONArray dArray = jObj.getJSONArray("d");
		
		ACLogger.info("CodeStock Downloader", "Iterating over speaker array");

		for (int i = 0; i <= dArray.length() - 1; i++) {
			Speaker newSpeaker = new Speaker();
			JSONObject speakerJSON = dArray.getJSONObject(i);
			newSpeaker.setSpeakerBio(speakerJSON.getString("Bio"));
			newSpeaker.setCompany(speakerJSON.getString("Company"));
			newSpeaker.setSpeakerName(speakerJSON.getString("Name"));
			newSpeaker.setSpeakerPhotoUrl(speakerJSON.getString("PhotoUrl"));
			newSpeaker.setId(speakerJSON.getLong("SpeakerID"));
			newSpeaker.setTwitterHandle(speakerJSON.getString("TwitterID"));
			String temp = speakerJSON.getString("Website");
			if (temp.equalsIgnoreCase("http://"))
				newSpeaker.setWebSite("");
			else
				newSpeaker.setWebSite(speakerJSON.getString("Website"));
			
			parsedSpeakers.add(newSpeaker);
		}
		ACLogger.info("CodeStock Downloader", "Finished iterating over speaker array");
		
	}


	private void getSessionData(String sessionsUrl) throws ClientProtocolException, IOException, JSONException {
		ACLogger.info("CodeStock Downloader", "getSessionData from " + sessionsUrl);
		
		DefaultHttpClient hc = new DefaultHttpClient();
		HttpGet hg = new HttpGet(sessionsUrl);
		
		HttpResponse response = hc.execute(hg);
		String queryResult = EntityUtils.toString(response.getEntity());
		JSONObject jObj;

		jObj = new JSONObject(queryResult);
		JSONArray dArray = jObj.getJSONArray("d");

		ACLogger.info("CodeStock Downloader", "Iterating over session array");

		for (int i = 0; i <= dArray.length() - 1; i++) {
			Session newSession = new Session();
			String savedArea = "";
			
			JSONObject sessionJSON = dArray.getJSONObject(i);
			newSession.setSynopsis(sessionJSON.getString("Abstract"));

			JSONArray additionalSpeakers = sessionJSON.getJSONArray("AdditionalSpeakerIDs");
			if (additionalSpeakers != null && additionalSpeakers.length() > 0) {
				for (int j = 0; j <= additionalSpeakers.length() - 1; j++) {
					long speakerID = additionalSpeakers.getLong(j);
					Speaker addSpeaker = findParsedSpeaker(speakerID);
					newSession.addAdditionalSpeaker(addSpeaker);
				}
			}
			savedArea = sessionJSON.getString("Area");
			newSession.setEndDate(convertToCalendar(sessionJSON.getString("EndTime")));
			newSession.setGeneralExperienceLevel(findOrCreateExperienceLevel(sessionJSON.getString("LevelGeneral")));
			newSession.setSpecificExperienceLevel(findOrCreateExperienceLevel(sessionJSON.getString("LevelSpecific")));
			newSession.setRoom(sessionJSON.getString("Room"));
			newSession.setId(sessionJSON.getLong("SessionID"));
			newSession.setSpeaker(findParsedSpeaker(sessionJSON.getLong("SpeakerID")));
			newSession.setStartDate(convertToCalendar(sessionJSON.getString("StartTime")));
			newSession.setTechnologies(sessionJSON.getString("Technology"));
			newSession.setSessionTitle(sessionJSON.getString("Title"));
			newSession.setTrack(findOrCreateTrack(sessionJSON.getString("Track") + savedArea));
			newSession.setVoteRank(sessionJSON.getString("VoteRank"));
			
			parsedSessions.add(newSession);
		}
		ACLogger.info("CodeStock Downloader", "Finished iterating over session array");
		
	}
	
	private Track findOrCreateTrack(String trackName) {
		Track theTrack = null;
		for (Track t : parsedTracks) {
			if (t.getTrackTitle().equalsIgnoreCase(trackName)) {
				theTrack = t;
				break;
			}
		}
		if (theTrack == null) {
			theTrack = new Track();
			theTrack.setTrackTitle(trackName);
			parsedTracks.add(theTrack);
		}
		return theTrack;
	}
	
	private ExperienceLevel findOrCreateExperienceLevel(String levelString) {
		ExperienceLevel theLevel = null;
		for (ExperienceLevel l : parsedLevels) {
			if (l.getLevelName().equalsIgnoreCase(levelString)) {
				theLevel = l;
				break;
			}
		}
		
		if (theLevel == null) {
			theLevel = new ExperienceLevel();
			theLevel.setLevelName(levelString);
			parsedLevels.add(theLevel);
		}
			
		return theLevel;
	}
	
	
	private Calendar convertToCalendar(String dateString) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(dateString.substring(6, 19)));
		cal.setTimeZone(TimeZone.getTimeZone("GMT" + dateString.substring(19, 24)));
		return cal;
	}
	
	private Speaker findParsedSpeaker(long speakerid) {
		
		for (Speaker s : parsedSpeakers) {
			if (s.getId() == speakerid) {
				return s;
			}
		}
		return null;
	}
}
