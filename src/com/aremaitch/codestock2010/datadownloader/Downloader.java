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
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;
import android.util.Log;

import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;

public class Downloader {
	Context context = null;
	String url = "";
		
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


	public Downloader(Context currentContext, String url) {
		this.context = currentContext;
		this.url = url;
	}

	
	/**
	 * Query the web site and download the current data.
	 * <p>
	 * Note this method runs on a background thread and should not attempt to manipulate any UI.
	 * 
	 * @return
	 */
	public boolean getCodeStockData() {
		// InputStream in = null;
		// try {
		// in = this.getInputStream();
		// } catch (IOException e) {
		//
		// e.printStackTrace();
		// }
		// if (in == null) {
		// return false;
		// }

		//	Note: the data coming from the feed is not normalized. If a speaker has three sessions their speaker
		//		data will appear three times; once for each session.
		
		JsonParser jp = null;
		JsonFactory f = new JsonFactory();
		try {
			// There is an overload to createJsonParser() that accepts a URL,
			// thus eliminating the need for the InputStream
			// jp = f.createJsonParser(in);
			// However, if Michael ever implements http compression we would need to switch back to
			// creating an InputStream to take advantage of it.

			jp = f.createJsonParser(new URL(this.url));

			JsonToken jt = jp.nextToken(); // should be start object

			jp.nextToken();
			if (!jp.getCurrentName().equalsIgnoreCase("d")) {
				// Problem: first element should be 'd' because of MS screwing
				// with json format.
				throw new JsonParseException("Error parsing data; first element is not 'd'", jp.getCurrentLocation());
			}

			jp.nextToken(); // should be another start array (the array of 'd')

			//	Is there enough memory to build the entire object graph in memory?
			//	Otherwise I would have to intersperse the db update code with the download and
			//	json parsing code.
			//	The data has no primary key.
			//	I could assign a temporary id (which would be meaningless when the db is updated)
			//	
			
			while (jp.nextToken() != JsonToken.END_ARRAY) {

				Session newSession = new Session();
				
				//	Note: we are combining track + area into one entity.
				String savedArea = "";
				
				while (jp.nextToken() != JsonToken.END_OBJECT) {
					String fieldName = "";
					String text = "";

					fieldName = jp.getCurrentName();

					if (fieldName == null) {
						continue;
					}

					if (fieldName.equalsIgnoreCase("__type")) {
						// MS WCF type; just eat the value and continue.
						jp.nextToken();
						continue;
					}

					if (fieldName.equalsIgnoreCase("additionalspeakers")) {
						// This is an array of speakers that is normally empty
						// will need a subloop to read the speaker objects
						
						jp.nextToken();		// this will move to the start of the array
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							//	Now we should be pointing at the START_OBJECT of the next additional speaker, if any
							//	parseSpeaker() will advance through the fields of the object.
							newSession.addAdditionalSpeaker(parseSpeaker(jp));
							//	Upon return, we will be pointing at END_OBJECT.
							//	Next iteration will either advance to START_OBJECT of the next speaker
							//		or END_ARRAY if there are no more speakers.
						}
					}

					if (fieldName.equalsIgnoreCase("abstract")) {
						jp.nextToken();
						newSession.setSynopsis(jp.getText()); // this should be the session
												// abstract text
//						Log.v("CodeStock Downloader", jp.getText().substring(0, 20));
					}

					if (fieldName.equalsIgnoreCase("area")) {
						jp.nextToken();
						savedArea = jp.getText();
					}

					//	The data is in the format: '/Date(9999999999999STTTT)/' where
					//	the 1st 13 digits is the milliseconds since 1/1/1070, S is a
					//	sign (+ or -) and TTTT is the timezone offset from GMT
					if (fieldName.equalsIgnoreCase("endtime")) {
						jp.nextToken();
						text = jp.getText(); // this should be the session end
												// time in some format I'll need
												// to decode)
						
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(Long.parseLong(text.substring(6, 19)));
						cal.setTimeZone(TimeZone.getTimeZone("GMT" + text.substring(19, 24)));
						newSession.setEndDate(cal);
					}

					if (fieldName.equalsIgnoreCase("starttime")) {
						jp.nextToken();
						text = jp.getText();
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(Long.parseLong(text.substring(6,19)));
						cal.setTimeZone(TimeZone.getTimeZone("GMT" + text.substring(19,24)));
						newSession.setStartDate(cal);
					}

					if (fieldName.equalsIgnoreCase("levelgeneral")) {
						jp.nextToken();
						text = jp.getText();
						if (!isXPLevelInParsedLevelsList(text)) {
							ExperienceLevel newLevel = new ExperienceLevel();
							newLevel.setLevelName(text);
							parsedLevels.add(newLevel);
							newSession.setGeneralExperienceLevel(newLevel);
						} else {
							newSession.setGeneralExperienceLevel(findXPLevelInParsedLevelsList(text));
						}
					}

					if (fieldName.equalsIgnoreCase("levelspecific")) {
						jp.nextToken();
						text = jp.getText();
						if (!isXPLevelInParsedLevelsList(text)) {
							ExperienceLevel newLevel = new ExperienceLevel();
							newLevel.setLevelName(text);
							parsedLevels.add(newLevel);
							newSession.setSpecificExperienceLevel(newLevel);
						} else {
							newSession.setSpecificExperienceLevel(findXPLevelInParsedLevelsList(text));
						}
					}

					if (fieldName.equalsIgnoreCase("room")) {
						jp.nextToken();
						newSession.setRoom(jp.getText()); // this should be the assigned room
												// for the session
					}


					if (fieldName.equalsIgnoreCase("technology")) {
						jp.nextToken();
						newSession.setTechnologies(jp.getText()); // this should be the technology
												// description
					}

					if (fieldName.equalsIgnoreCase("title")) {
						jp.nextToken();
						newSession.setSessionTitle(jp.getText()); // this should be the session title
					}

					if (fieldName.equalsIgnoreCase("track")) {
						jp.nextToken();
						text = jp.getText() + ": " + savedArea;
						if (!isTrackInParsedTracksList(text)) {
							Track newTrack = new Track();
							newTrack.setTrackTitle(text);
							parsedTracks.add(newTrack);
							newSession.setTrack(newTrack);
						} else {
							newSession.setTrack(findTrackInParsedTracksList(text));
						}
						savedArea = "";
					}

					if (fieldName.equalsIgnoreCase("speaker")) {
						// start of the speaker object; the next token should be
						// a start object
						jp.nextToken();
						
						newSession.setSpeaker(parseSpeaker(jp));
					}
				} // end of session object
				
				parsedSessions.add(newSession);
				
			} // end of array

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (jp != null) {
					jp.close();
				}
			} catch (IOException e) {
				// Just eat it.
			}
		}

		return true;
	}


	/**
	 * On exit the JsonParser is left pointing at the JsonToken.END_OBJECT.
	 * 
	 * @param jp - JsonParser being parsed. Expects to be pointing at JsonToken.START_OBJECT
	 * @throws JsonParseException
	 * @throws IOException
	 * 
	 */
	
	private Speaker parseSpeaker(JsonParser jp) throws JsonParseException, IOException {
		// Expense on entry to be pointing at the JsonToken.START_OBJECT
		//	
		Speaker spkr = new Speaker();
		
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String sFieldName = jp.getCurrentName();

			if (sFieldName == null) {
				Log.e("parseSpeaker", "sFieldName is null");
			}
			if (sFieldName.equalsIgnoreCase("__type")) {
				jp.nextToken();
				continue;
			}

			if (sFieldName.equalsIgnoreCase("bio")) {
				jp.nextToken();
				spkr.setSpeakerBio(jp.getText()); // should be the speaker's
										// biography
			}

			if (sFieldName.equalsIgnoreCase("company")) {
				jp.nextToken();
				spkr.setCompany(jp.getText()); // should be the speaker's
										// company
			}

			if (sFieldName.equalsIgnoreCase("name")) {
				jp.nextToken();
				spkr.setSpeakerName(jp.getText()); // should be the speaker's
										// anme
			}

			if (sFieldName.equalsIgnoreCase("photourl")) {
				jp.nextToken();
				spkr.setSpeakerPhotoUrl(jp.getText()); // should be a url
										// pointing to the
										// speaker's photo
			}

			if (sFieldName.equalsIgnoreCase("twitterid")) {
				jp.nextToken();
				spkr.setTwitterHandle(jp.getText()); // should be the speaker's
										// Twitter handle
			}

			if (sFieldName.equalsIgnoreCase("website")) {
				jp.nextToken();
				spkr.setWebSite(jp.getText()); // should be the speaker's
										// web site url
			}
		}
		if (!isSpeakerNameInParsedSpeakerList(spkr.getSpeakerName())) {
			parsedSpeakers.add(spkr);
		}
		return spkr;
	}

	private boolean isSpeakerNameInParsedSpeakerList(String speakerName) {
		//	If null is returned we didn't find the speaker so return false;
		//	If a valid object is returned it is the speaker so return true;
		return findSpeakerInParsedSpeakerList(speakerName) != null;
	}

	private Speaker findSpeakerInParsedSpeakerList(String speakerName) {
		//	Scan the list of speakers we've already parsed out and see if the passed name
		//	is already in the list. If so, return the found speaker and short-circuit the iterator.
		//	Otherwise, return null.
		Speaker found = null;
		for (Speaker test : parsedSpeakers) {
			if (test.getSpeakerName().equalsIgnoreCase(speakerName)) {
				found = test;
				break;
			}
		}
		return found;
	}
	
	
	private boolean isTrackInParsedTracksList(String trackName) {
		return findTrackInParsedTracksList(trackName) != null;
	}

	private Track findTrackInParsedTracksList(String trackName) {
		Track found = null;
		for (Track test : parsedTracks) {
			if (test.getTrackTitle().equalsIgnoreCase(trackName)) {
				found = test;
				break;
			}
		}
		return found;
	}
	
	private boolean isXPLevelInParsedLevelsList(String xpLevel) {
		return findXPLevelInParsedLevelsList(xpLevel) != null;
	}

	private ExperienceLevel findXPLevelInParsedLevelsList(String xpLevel) {
		ExperienceLevel found = null;
		for (ExperienceLevel test : parsedLevels) {
			if (test.getLevelName().equalsIgnoreCase(xpLevel)) {
				found = test;
				break;
			}
		}
		return found;
	}
	
//	private InputStream getInputStream() throws IOException {
//		InputStream inStream = null;
//
//		// URL ctor can throw MalformedURLException; not likely here as the URL
//		// is hard-coded
//		URL url;
//		try {
//			url = new URL(this.url);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			return inStream;
//		}
//
//		// openConnection() can throw IOException; let that one bubble up to
//		// caller.
//		URLConnection urlConn = url.openConnection();
//
//		if (!(urlConn instanceof HttpURLConnection)) {
//			throw new IOException(
//					"Could not retrieve data; connection is not HTTP");
//		}
//
//		HttpURLConnection httpConn = (HttpURLConnection) urlConn;
//		// setAllowUserInteraction() determines whether it makes sense to allow
//		// user-interaction
//		// (such as popping up an authentication dialog.) In this case, no it
//		// does not make sense.
//		// (It's actually inherited from URLConnection.)
//		httpConn.setAllowUserInteraction(false);
//		// setInstanceFollowRedirects() determines whether we automatically
//		// follow Http redirects
//		// (response codes 3xx). If the URL for the data changes and the current
//		// URL implements
//		// a 3xx response code, our connection here should follow automatically.
//		httpConn.setInstanceFollowRedirects(true);
//		// setRequestMethod() sets the Http request method. The default is GET
//		// but set it explicitly
//		// here (you know what they say about assuming.)
//		httpConn.setRequestMethod("GET");
//		// httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
//		httpConn.connect();
//
//		// Get the response code
//		int resCode = httpConn.getResponseCode();
//		if (resCode == HttpURLConnection.HTTP_OK) {
//			// Looks good; get the input stream (getInputStream() is inherited
//			// from URLConnection)
//			inStream = httpConn.getInputStream();
//		}
//		return inStream;
//	}
}
