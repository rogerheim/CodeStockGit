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

import android.content.Context;
import android.widget.Toast;
import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;
import com.aremaitch.utils.ACLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * Date: 2/10/11
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgendaParser {
    private final IAgendaDownloader _agendaDownloader;
    private List<Speaker> parsedSpeakers = new ArrayList<Speaker>();
    private List<Session> parsedSessions = new ArrayList<Session>();
    private List<ExperienceLevel> parsedLevels = new ArrayList<ExperienceLevel>();
    private List<Track> parsedTracks = new ArrayList<Track>();
    private boolean isError = false;

    public List<Speaker> getParsedSpeakers() {
        return parsedSpeakers;
    }

    public List<Session> getParsedSessions() {
        return parsedSessions;
    }

    public List<ExperienceLevel> getParsedLevels() {
        return parsedLevels;
    }

    public List<Track> getParsedTracks() {
        return parsedTracks;
    }

    public AgendaParser(IAgendaDownloader agendaDownloader) {
        _agendaDownloader = agendaDownloader;
    }

    public boolean isError() {
        return this.isError;
    }

    public void doGetData() {

        try {
            JSONObject speakerObj = _agendaDownloader.getSpeakerData();
            if (speakerObj == null) {
                ACLogger.error(CSConstants.LOG_TAG, "error getting speaker data: could not get speaker list");
                isError = true;
                return;
            }
            JSONArray dataArray = speakerObj.getJSONArray("d");
            parseSpeakerData(dataArray);
        } catch (JSONException e) {
            ACLogger.error(CSConstants.LOG_TAG, "error getting speaker data: 'd' array not found");
            isError = true;
        }

        try {
            JSONObject sessionObj = _agendaDownloader.getSessionData();
            if (sessionObj == null) {
                ACLogger.error(CSConstants.LOG_TAG, "error getting session data: could not get session list");
                isError = true;
                return;
            }
            JSONArray dataArray = sessionObj.getJSONArray("d");
            parseSessionData(dataArray);
        } catch (JSONException e) {
            ACLogger.error(CSConstants.LOG_TAG, "error getting session data: 'd' array not found");
            isError = true;
        }

    }

    public void parseSpeakerData(JSONArray dataArray) {

        ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "parsing speaker data");
        for (int i = 0; i <= dataArray.length() - 1; i++) {
            try {
                Speaker newSpeaker = new Speaker();
                JSONObject speakerJSONObject = dataArray.getJSONObject(i);
                newSpeaker.setSpeakerBio(speakerJSONObject.getString("Bio"));
                newSpeaker.setCompany(speakerJSONObject.getString("Company"));
                newSpeaker.setSpeakerName(speakerJSONObject.getString("Name"));
                newSpeaker.setSpeakerPhotoUrl(speakerJSONObject.getString("PhotoUrl"));
                newSpeaker.setId(speakerJSONObject.getLong("SpeakerID"));
                newSpeaker.setTwitterHandle(speakerJSONObject.getString("TwitterID"));
                String website = speakerJSONObject.getString("Website");
                if (website.equalsIgnoreCase("http://"))
                    newSpeaker.setWebSite("");
                else
                    newSpeaker.setWebSite(website);

                parsedSpeakers.add(newSpeaker);
            } catch (JSONException e) {
                ACLogger.error(CSConstants.LOG_TAG, "error parsing speaker data: " + e.getMessage());
                isError = true;
            }
        }
    }

    public void parseSessionData(JSONArray dataArray) {
        ACLogger.info(CSConstants.AGENDADWNLDSVC_LOG_TAG, "parsing session data");

        for (int i = 0; i <= dataArray.length() - 1; i++) {
            try {
                Session newSession = new Session();
                String savedArea;
                JSONObject sessionJSONObject = dataArray.getJSONObject(i);
                newSession.setSynopsis(sessionJSONObject.getString("Abstract"));

                JSONArray additionalSpeakers = sessionJSONObject.getJSONArray("AdditionalSpeakerIDs");
                if (additionalSpeakers != null && additionalSpeakers.length() > 0) {
                    for (int j = 0; j <= additionalSpeakers.length() - 1; j++) {
                        long speakerID = additionalSpeakers.getLong(j);
                        Speaker addSpeaker = findParsedSpeaker(speakerID);
                        newSession.addAdditionalSpeaker(addSpeaker);
                    }
                }
                savedArea = sessionJSONObject.getString("Area");
                newSession.setEndDate(convertToCalendar(sessionJSONObject.getString("EndTime")));
                newSession.setGeneralExperienceLevel(findOrCreateExperienceLevel(sessionJSONObject.getString("LevelGeneral")));
                newSession.setSpecificExperienceLevel(findOrCreateExperienceLevel(sessionJSONObject.getString("LevelSpecific")));
                newSession.setRoom(sessionJSONObject.getString("Room"));
                newSession.setId(sessionJSONObject.getLong("SessionID"));
                newSession.setSpeaker(findParsedSpeaker(sessionJSONObject.getLong("SpeakerID")));
                newSession.setStartDate(convertToCalendar(sessionJSONObject.getString("StartTime")));
                newSession.setTechnologies(sessionJSONObject.getString("Technology"));
                newSession.setSessionTitle(sessionJSONObject.getString("Title"));

                //  Add a space between track and area.
                newSession.setTrack(findOrCreateTrack(String.format("%s %s", sessionJSONObject.getString("Track"), savedArea)));
                newSession.setVoteRank(sessionJSONObject.getString("VoteRank"));

                parsedSessions.add(newSession);
            } catch (JSONException e) {
                ACLogger.error(CSConstants.LOG_TAG, "error parsing session data: " + e.getMessage());
                isError = true;
            }
        }
    }

    private Track findOrCreateTrack(String trackName) {
        Track track = null;
        for (Track t : parsedTracks) {
            if (t.getTrackTitle().equalsIgnoreCase(trackName)) {
                track = t;
                break;
            }
        }
        if (track == null) {
            track = new Track();
            track.setTrackTitle(trackName);
            parsedTracks.add(track);
        }
        return track;
    }

    private ExperienceLevel findOrCreateExperienceLevel(String levelString) {
        ExperienceLevel level = null;
        for (ExperienceLevel l : parsedLevels) {
            if (l.getLevelName().equalsIgnoreCase(levelString)) {
                level = l;
                break;
            }
        }

        if (level == null) {
            level = new ExperienceLevel();
            level.setLevelName(levelString);
            parsedLevels.add(level);
        }
        return level;
    }

    private Calendar convertToCalendar(String dateString) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(dateString.substring(6, 19)));
        cal.setTimeZone(TimeZone.getTimeZone("GMT" + dateString.substring(19, 24)));
        return cal;
    }

    private Speaker findParsedSpeaker(long speakerID) {
        for (Speaker s : parsedSpeakers) {
            if (s.getId() == speakerID)
                return s;
        }
        return null;
    }

}
