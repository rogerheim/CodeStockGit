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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.aremaitch.codestock2010.R;
import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;
import com.aremaitch.utils.ACLogger;

import android.content.Context;

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
	
	private void getSpeakerData(String speakersUrl) throws JsonParseException, MalformedURLException, IOException {
		ACLogger.info("CodeStock Downloader", "getSpeakerData from " + speakersUrl);
		JsonParser jp = null;
		JsonFactory f = new JsonFactory();
		
		try {
			jp = f.createJsonParser(new URL(speakersUrl));
			
			//	Skip to start object
			jp.nextToken();
			//	Skip to Microsoft 'd'
			jp.nextToken();
			
			if (!jp.getCurrentName().equalsIgnoreCase("d")) {
				throw new JsonParseException("Error parsing speaker data; first element is not 'd'", jp.getCurrentLocation());
			}
			
			//	Start array (the array of 'd')
			jp.nextToken();
			
			//	Version 2 api includes site-generated primary key that is (hopefully) stable.
			while (jp.nextToken() != JsonToken.END_ARRAY) {
				Speaker newSpeaker = new Speaker();
				
				while (jp.nextToken() != JsonToken.END_OBJECT) {
					String fieldName = "";
					String text = "";
					
					fieldName = jp.getCurrentName();
					if (fieldName == null)
						continue;
					
					if (fieldName.equalsIgnoreCase("__type")) {
						//	MS WCF type; eat it and continue
						jp.nextToken();
						continue;
					} else if (fieldName.equalsIgnoreCase("bio")) {
						jp.nextToken();
						newSpeaker.setSpeakerBio(jp.getText());
					} else if (fieldName.equalsIgnoreCase("company")) {
						jp.nextToken();
						newSpeaker.setCompany(jp.getText());
					} else if (fieldName.equalsIgnoreCase("name")) {
						jp.nextToken();
						newSpeaker.setSpeakerName(jp.getText());
					} else if (fieldName.equalsIgnoreCase("photourl")) {
						jp.nextToken();
						newSpeaker.setSpeakerPhotoUrl(jp.getText());
					} else if (fieldName.equalsIgnoreCase("speakerid"))	{
						jp.nextToken();
						newSpeaker.setId(jp.getLongValue());
					} else if (fieldName.equalsIgnoreCase("twitterid")) {
						jp.nextToken();
						newSpeaker.setTwitterHandle(jp.getText());
					} else if (fieldName.equalsIgnoreCase("website")) {
						jp.nextToken();
						text = jp.getText();
						//	Another little tweak: if the speaker did not provide a web site, the value from the feed
						//	will be just 'http://' instead of null.
						if (text.equalsIgnoreCase("http://")) {
							newSpeaker.setWebSite("");
						} else {
							newSpeaker.setWebSite(text);
						}
					}
				}	// end of speaker object
				
				parsedSpeakers.add(newSpeaker);
			}	// end of array of speakers
		} finally {
			try {
				if (jp != null) {
					jp.close();
				}
			} catch (IOException e) {
				// just eat it
			}
		}
	}

	private void getSessionData(String sessionsUrl) throws JsonParseException, MalformedURLException, IOException {
		ACLogger.info("CodeStock Downloader", "getSessionData from " + sessionsUrl);
		JsonParser jp = null;
		JsonFactory f = new JsonFactory();
		
		try {
			jp = f.createJsonParser(new URL(sessionsUrl));
			
			//	Skip to start object
			jp.nextToken();
			//	Skip to Microsoft 'd'
			jp.nextToken();
			
			if (!jp.getCurrentName().equalsIgnoreCase("d")) {
				throw new JsonParseException("Error parsing speaker data; first element is not 'd'", jp.getCurrentLocation());
			}
			
			//	Start array (the array of 'd')
			jp.nextToken();
			
			//	Version 2 api includes site-generated primary key that is (hopefully) stable.
			while (jp.nextToken() != JsonToken.END_ARRAY) {
				Session newSession = new Session();
				String savedArea = "";
				
				while (jp.nextToken() != JsonToken.END_OBJECT) {
					String fieldName = "";
					String text = "";
					
					fieldName = jp.getCurrentName();
					if (fieldName == null)
						continue;
					
					if (fieldName.equalsIgnoreCase("__type")) {
						//	MS WCF type; eat it and continue
						jp.nextToken();
						continue;
					} else if (fieldName.equalsIgnoreCase("abstract")) {
						jp.nextToken();
						newSession.setSynopsis(jp.getText());
					} else if (fieldName.equalsIgnoreCase("additionalspeakerids")) {
						jp.nextToken();	// start of array
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							Speaker addSpeaker = findParsedSpeaker(jp.getLongValue());
							newSession.addAdditionalSpeaker(addSpeaker);
						}
					} else if (fieldName.equalsIgnoreCase("area")) {
						jp.nextToken();
						savedArea = jp.getText();
					} else if (fieldName.equalsIgnoreCase("endtime")) {
						jp.nextToken();
						newSession.setEndDate(convertToCalendar(jp.getText()));
					} else if (fieldName.equalsIgnoreCase("levelgeneral")) {
						jp.nextToken();
						newSession.setGeneralExperienceLevel(findOrCreateExperienceLevel(jp.getText()));
					} else if (fieldName.equalsIgnoreCase("levelspecific")) {
						jp.nextToken();
						newSession.setSpecificExperienceLevel(findOrCreateExperienceLevel(jp.getText()));
					} else if (fieldName.equalsIgnoreCase("room")) {
						jp.nextToken();
						newSession.setRoom(jp.getText());
					} else if (fieldName.equalsIgnoreCase("sessionid")) {
						jp.nextToken();
						newSession.setId(jp.getLongValue());
					} else if (fieldName.equalsIgnoreCase("speakerid")) {
						jp.nextToken();
						newSession.setSpeaker(findParsedSpeaker(jp.getLongValue()));
					} else if (fieldName.equalsIgnoreCase("starttime")) {
						jp.nextToken();
						newSession.setStartDate(convertToCalendar(jp.getText()));
					} else if (fieldName.equalsIgnoreCase("technology")) {
						jp.nextToken();
						newSession.setTechnologies(jp.getText());
					} else if (fieldName.equalsIgnoreCase("title")) {
						jp.nextToken();
						newSession.setSessionTitle(jp.getText());
					} else if (fieldName.equalsIgnoreCase("track")) {
						jp.nextToken();
						text = jp.getText() + ": " + savedArea;
						newSession.setTrack(findOrCreateTrack(text));
					} else if (fieldName.equalsIgnoreCase("voterank")) {
						jp.nextToken();
						newSession.setVoteRank(jp.getText());
					}
				}
				parsedSessions.add(newSession);
			}
		} finally {
			try {
				if (jp != null) {
					jp.close();
				}
			} catch (IOException e) {
				// just eat it
			}
		}
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
