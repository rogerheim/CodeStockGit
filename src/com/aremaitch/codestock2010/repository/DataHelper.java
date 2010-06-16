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

package com.aremaitch.codestock2010.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import org.joda.time.DateTime;
//import org.joda.time.format.ISODateTimeFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

//	8-Jun-10	Updated to version 2 for VoteRank in session table and add
//				additional speakers table.
/**
 * Data helper for SQL access.
 * First drafg: 25-May-2010
 * @author Roger Heim, Aremaitch Consulting
 * 
 *
 */
public class DataHelper {
	private static final String DATABASE_NAME = "codestock2010.db";
	private static final int DATABASE_VERSION = 2;
	private static final String XPLEVELS_TABLE_NAME = "xplevels";
	private static final String TRACKS_TABLE_NAME = "tracks";
	private static final String SPEAKERS_TABLE_NAME = "speakers";
	private static final String SESSIONS_TABLE_NAME = "sessions";
	private static final String ADD_SPEAKERS_TABLE_NAME = "addspeakers";
	private static final String LOG_TAG = "CodeStock2010";
	
	private Context context;
	private SQLiteDatabase db;
	
	
	public DataHelper(Context context) {
		this.context = context;
		openDatabase();
	}
	
	/**
	 * Tests the database to see if there is any data at all.
	 * @return True if there is no data; false otherwise.
	 */
	public boolean isDatabaseEmpty() {
		boolean isEmpty = true;
		boolean dbWasOpen = isOpen();
		
		Cursor c = null;
		try {
			c = db.rawQuery("select count(*) from " + SESSIONS_TABLE_NAME, null);
			c.moveToFirst();
			if (c.getInt(0) > 0) {
				isEmpty = false;
			}
		} finally {
			if (c != null) {
				c.close();
				if (!dbWasOpen) {
					db.close();
				}
			}
		}
		return isEmpty;
	}
	
	/**
	 * Closes the database if it is currently open.
	 */
	public void close() {
		if (isOpen()) {
			db.close();
			
		}
	}
	
	/**
	 * Opens the database if it is currently closed.
	 */
	public void openDatabase() {
		if (this.isOpen()){
			return;
		}
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}
	
	/**
	 * Tests to see if the database is currently open.
	 * @return True if the database is open; false otherwise.
	 */
	public boolean isOpen() {
		if (db != null) {
		return db.isOpen();
		} else {
			return false;
		}
	}
	
	/**
	 * Returns a list of all tracks.
	 * @return An ArrayList&lt;Track&gt; containing the conference tracks.
	 */
	public ArrayList<Track> getListOfTracks() {
		ArrayList<Track> tracks = new ArrayList<Track>();
		
		Cursor c = null;
		try {
			c = this.db.rawQuery("select id, tracktitle from " + TRACKS_TABLE_NAME + " order by tracktitle ", null);
			while (c.moveToNext()) {
				Track t = new Track();
				t.setId(c.getLong(0));
				t.setTrackTitle(c.getString(1));
				tracks.add(t);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return tracks;
	}
	
	/**
	 * Returns a list of MiniSessions (session id, title, start date & time, room, and speaker's name)
	 * suitable for ListViews and other places where the full session/speaker details are not required.
	 * 
	 * @param trackid	The id of the track whose sessions we want.
	 * @return ArrayList&lt;MiniSession&gt;
	 */
	public ArrayList<MiniSession> getListOfMiniSessions(long trackid) {
		ArrayList<MiniSession> sessions = new ArrayList<MiniSession>();
		
		// 9-Jun-10: Changed 'award' to 'voterank'
		Cursor c = null;
		try {
			c = this.db.rawQuery("select sessions.id, sessiontitle, voterank, startdatetime, room, speakers.speakername " +
								"from " + SESSIONS_TABLE_NAME + 
									" inner join " + SPEAKERS_TABLE_NAME + " on speakers.id = sessions.fkspeaker " +
								"where fktrack = ? order by sessiontitle", 
					new String[] {Long.toString(trackid)});
			while (c.moveToNext()) {
				MiniSession s = new MiniSession();
				s.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				s.setSessionTitle(c.getString(c.getColumnIndexOrThrow("sessiontitle")));
				s.setVoteRank(c.getString(c.getColumnIndexOrThrow("voterank")));
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("startdatetime"))));
				s.setStartDateTime(cal);
				
				s.setRoom(c.getString(c.getColumnIndexOrThrow("room")));
				s.setSpeakerName(c.getString(c.getColumnIndexOrThrow("speakername")));
				sessions.add(s);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return sessions;
	}
	
	/**
	 * Returns a list of sessions in a specific timeslot.
	 * @param
	 * desiredTimeSlot <br>A Calendar object containing the desired timeslot.
	 * @return An ArrayList&lt;AgendaSession&gt; containing the sessions sorted by room number
	 * or an empty ArrayList if there are no sessions in the desired timeslot.
	 */
	public ArrayList<AgendaSession> getAgendaSessionsInTimeslot(Calendar desiredTimeSlot) {
		ArrayList<AgendaSession> sessions = new ArrayList<AgendaSession>();
		
		Cursor c = null;
		try {
			c = db.rawQuery("select sessions.id, sessiontitle, voterank, startdatetime, room, speakers.speakername, " +
					"tracks.tracktitle " +
					"from sessions inner join speakers on speakers.id = sessions.fkspeaker, " +
					"tracks on tracks.id = sessions.fktrack " +
					"where startdatetime = ? " +
					"order by room", new String[] {Long.toString(desiredTimeSlot.getTimeInMillis())});
			while (c.moveToNext()) {
				AgendaSession s = new AgendaSession();
				s.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				s.setSessionTitle(c.getString(c.getColumnIndexOrThrow("sessiontitle")));
				s.setVoteRank(c.getString(c.getColumnIndexOrThrow("voterank")));
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("startdatetime"))));
				s.setStartDateTime(cal);
				
				s.setRoom(c.getString(c.getColumnIndexOrThrow("room")));
				s.setSpeakerName(c.getString(c.getColumnIndexOrThrow("speakername")));
				s.setTrackName(c.getString(c.getColumnIndexOrThrow("tracktitle")));
				sessions.add(s);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return sessions;
	}

	
	public ArrayList<AgendaSession> getListOfAgendaSessionsChronologically() {
		ArrayList<AgendaSession> sessions = new ArrayList<AgendaSession>();
		
		Cursor c = null;
		try {
			c = db.rawQuery("select sessions.id, sessiontitle, voterank, startdatetime, room, speakers.speakername, " +
					"tracks.tracktitle " +
					"from sessions inner join speakers on speakers.id = sessions.fkspeaker, " +
					"tracks on tracks.id = sessions.fktrack " +
					"order by startdatetime, room", null);
			while (c.moveToNext()) {
				AgendaSession s = new AgendaSession();
				s.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				s.setSessionTitle(c.getString(c.getColumnIndexOrThrow("sessiontitle")));
				s.setVoteRank(c.getString(c.getColumnIndexOrThrow("voterank")));
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("startdatetime"))));
				s.setStartDateTime(cal);
				
				s.setRoom(c.getString(c.getColumnIndexOrThrow("room")));
				s.setSpeakerName(c.getString(c.getColumnIndexOrThrow("speakername")));
				s.setTrackName(c.getString(c.getColumnIndexOrThrow("tracktitle")));
				sessions.add(s);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return sessions;
	}

	
	public ArrayList<MiniSession> getListOfMiniSessionsFromListOfIDS(ArrayList<Long> listOfIds) {
		ArrayList<MiniSession> sessions = new ArrayList<MiniSession>();
		
		Cursor c = null;
		try {
			c = db.rawQuery("select sessions.id, sessiontitle, voterank, startdatetime, room, speakers.speakername from sessions inner join speakers on speakers.id = sessions.fkspeaker " +
					"where sessions.id in (" + TextUtils.join(",", listOfIds) + ") order by startdatetime", null);
			while (c.moveToNext()) {
				MiniSession s = new MiniSession();
				s.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				s.setSessionTitle(c.getString(c.getColumnIndexOrThrow("sessiontitle")));
				s.setVoteRank(c.getString(c.getColumnIndexOrThrow("voterank")));
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("startdatetime"))));
				s.setStartDateTime(cal);
				
				s.setRoom(c.getString(c.getColumnIndexOrThrow("room")));
				s.setSpeakerName(c.getString(c.getColumnIndexOrThrow("speakername")));
				sessions.add(s);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		
		
		
		return sessions;
	}
	
//	private String toInPhrase(ArrayList<Long> theList) {
//		StringBuilder work = new StringBuilder();
//		String result = "";
//		for (Long l : theList) {
//			work.append(l).append(',');
//		}
//		result = work.toString();
//		
//		return TextUtils.join(",", theList);
//	
//	}
	
	/**
	 * Returns full details about a session.
	 * @param sessionid  The id of the session whose information we want.
	 * @return A Session object containing the session details.
	 */
	public Session getSession(long sessionid) {
		Session result = null;
		
		Cursor c = null;
		try {
			c = this.db.rawQuery(
					"select * from sessions where id = ?", new String[] {Long.toString(sessionid)});
			if (c.moveToFirst()) {
				//	Bug fix: can't reuse cal object for boh dates because setting the object the second time
				//	will change the first reference.
				Calendar calStart = Calendar.getInstance();
				Calendar calEnd = Calendar.getInstance();
				result = new Session();
				result.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				result.setSessionTitle(c.getString(c.getColumnIndexOrThrow("sessiontitle")));
				result.setSynopsis(c.getString(c.getColumnIndexOrThrow("synopsis")));
				result.setTrack(getTrack(c.getLong(c.getColumnIndexOrThrow("fktrack"))));
				result.setSpeaker(getSpeaker(c.getLong(c.getColumnIndexOrThrow("fkspeaker"))));
				result.setGeneralExperienceLevel(getXPLevel(c.getLong(c.getColumnIndexOrThrow("fkgeneralxplevel"))));
				result.setSpecificExperienceLevel(getXPLevel(c.getLong(c.getColumnIndexOrThrow("fkspecificxplevel"))));
//				result.setAward(c.getString(c.getColumnIndexOrThrow("award")));
				result.setTechnologies(c.getString(c.getColumnIndexOrThrow("technologies")));
				calStart.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("startdatetime"))));
				result.setStartDate(calStart);
				calEnd.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("enddatetime"))));
				result.setEndDate(calEnd);
				result.setRoom(c.getString(c.getColumnIndexOrThrow("room")));
				result.setVoteRank(c.getString(c.getColumnIndexOrThrow("voterank")));
				
				result.setAdditionalSpeakers(getAdditionalSpeakers(c.getLong(c.getColumnIndexOrThrow("id"))));
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return result;
	}

	private List<Speaker> getAdditionalSpeakers(long sessionid) {
		ArrayList<Speaker> spkrs = new ArrayList<Speaker>();
		
		Cursor c = null;
		try {
			c = this.db.rawQuery(
					"select * from addspeakers where fksession = ?", new String[] {Long.toString(sessionid)});
			while (c.moveToNext()) {
				spkrs.add(getSpeaker(c.getLong(c.getColumnIndexOrThrow("fkspeaker"))));
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return spkrs;
	}
	
	/**
	 * Returns full details about a conference track.
	 * @param trackid	The id of the track whose information we want.
	 * @return A Track object containing the details about the track.
	 */
	public Track getTrack(long trackid) {
		Track result = null;
		
		Cursor c = null;
		try {
			c = this.db.rawQuery(
					"select * from tracks where id = ?", new String[] {Long.toString(trackid)});
			if (c.moveToFirst()) {
				result = new Track();
				result.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				result.setTrackTitle(c.getString(c.getColumnIndexOrThrow("tracktitle")));
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return result;
	}
	
	/**
	 * Returns full details about an experience level
	 * @param levelid	The id of the experience level we want.
	 * @return An ExperienceLevel object containing the details about the level.
	 */
	public ExperienceLevel getXPLevel(long levelid) {
		ExperienceLevel result = null;
		
		Cursor c = null;
		try {
			c = this.db.rawQuery(
					"select * from xplevels where id = ?", new String[] {Long.toString(levelid)});
			if (c.moveToFirst()) {
				result = new ExperienceLevel();
				result.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				result.setLevelName(c.getString(c.getColumnIndexOrThrow("levelname")));
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return result;
	}
	
	/**
	 * Returns full details about a speaker.
	 * @param speakerid	The id of the speaker we want.
	 * @return A Speaker object containing the details about the speaker.
	 */
	public Speaker getSpeaker(long speakerid) {
		Speaker result = null;
		Cursor c = null;
		try {
			c = this.db.rawQuery(
					"select * from speakers where id = ?", new String[] {Long.toString(speakerid)});
			if (c.moveToFirst()) {
				result = new Speaker();
				result.setId(c.getLong(c.getColumnIndexOrThrow("id")));
				result.setSpeakerName(c.getString(c.getColumnIndexOrThrow("speakername")));
				result.setSpeakerBio(c.getString(c.getColumnIndexOrThrow("speakerbio")));
				result.setTwitterHandle(c.getString(c.getColumnIndexOrThrow("twitterhandle")));
				result.setCompany(c.getString(c.getColumnIndexOrThrow("company")));
				result.setWebSite(c.getString(c.getColumnIndexOrThrow("website")));
				result.setSpeakerPhotoUrl(c.getString(c.getColumnIndexOrThrow("photourl")));
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return result;
	}
	
	/**
	 * Insert an experience level into the database.
	 * @param newLevel	An Experiencelevel object containing the data we want to insert.
	 * @return	The id of the newly inserted experience level.
	 */
	public long insertXPLevel(ExperienceLevel newLevel) {
		Log.v(LOG_TAG, "insertXPLevel:" + newLevel.getLevelName());
		ContentValues newRow = new ContentValues();
		newRow.put("levelname", newLevel.getLevelName());
		return db.insert(XPLEVELS_TABLE_NAME, null, newRow);
	}
	
	/**
	 * Insert a conference track into the database.
	 * @param newTrack	A Track object containing the data we want to insert.
	 * @return	The id of the newly inserted track.
	 */
	public long insertTrack(Track newTrack) {
		Log.v(LOG_TAG, "insertTrack:" + newTrack.getTrackTitle());
		ContentValues newRow = new ContentValues();
		newRow.put("tracktitle", newTrack.getTrackTitle());
		return db.insert(TRACKS_TABLE_NAME, null, newRow);
	}
	
	/**
	 * Insert a speaker into the database.
	 * @param newSpeaker	A Speaker object containing the data we want to insert.
	 * @return	The id of the newly inserted speaker.
	 */
	public long insertSpeaker(Speaker newSpeaker) {
		Log.v(LOG_TAG, "insertSpeaker:" + newSpeaker.getSpeakerName());
		ContentValues newRow = new ContentValues();
		
		//	Speaker ID is now set by the host service
		if (newSpeaker.getId() != 0) {
			newRow.put("id", newSpeaker.getId());
		}
		newRow.put("speakername", newSpeaker.getSpeakerName());
		newRow.put("speakerbio", newSpeaker.getSpeakerBio());
		newRow.put("twitterhandle", newSpeaker.getTwitterHandle());
		newRow.put("company", newSpeaker.getCompany());
		newRow.put("website", newSpeaker.getWebSite());
		newRow.put("photourl", newSpeaker.getSpeakerPhotoUrl());
		return db.insert(SPEAKERS_TABLE_NAME, null, newRow);
	}
	
	/**
	 * Insert a session into the database.
	 * @param newSession	A Session object containing the data we want to insert.
	 * @return	The id of the newly inserted session.
	 */
	public long insertSession(Session newSession) {
		Log.v(LOG_TAG, "insertSession:" + newSession.getSessionTitle());
		ContentValues newRow = new ContentValues();
		
		//	Session ID is now set by the host service
		if (newSession.getId() != 0) {
			newRow.put("id", newSession.getId());
		}
		newRow.put("sessiontitle", newSession.getSessionTitle());
		newRow.put("synopsis", newSession.getSynopsis());
		newRow.put("fktrack", getOrAddTrack(newSession.getTrack()));
		newRow.put("fkspeaker", getOrAddSpeaker(newSession.getSpeaker()));
		newRow.put("fkgeneralxplevel", getOrAddXPLevel(newSession.getGeneralExperienceLevel()));
		newRow.put("fkspecificxplevel", getOrAddXPLevel(newSession.getSpecificExperienceLevel()));
//		newRow.put("award", newSession.getAward());
		newRow.put("technologies", newSession.getTechnologies());
		newRow.put("startdatetime", String.valueOf(newSession.getStartDate().getTimeInMillis()));
		newRow.put("enddatetime", String.valueOf(newSession.getEndDate().getTimeInMillis()));
		newRow.put("room", newSession.getRoom());
		newRow.put("voterank", newSession.getVoteRank());

		Long sessionID = db.insert(SESSIONS_TABLE_NAME, null, newRow);
		//	support additional speakers
		if (newSession.getAdditionalSpeakers().size() > 0) {
			for (Speaker s : newSession.getAdditionalSpeakers()) {
				ContentValues newAddSpeakers = new ContentValues();
				newAddSpeakers.put("fksession", sessionID);
				newAddSpeakers.put("fkspeaker", s.getId());
				db.insert(ADD_SPEAKERS_TABLE_NAME, null, newAddSpeakers);
				
			}
		}
		return sessionID;
	}
	
	/**
	 * Drops all data and tables from the database.
	 */
	public void clearAllData() {
		Log.v(LOG_TAG, "Clearing data");
		db.delete(SESSIONS_TABLE_NAME, null, null);
		db.delete(SPEAKERS_TABLE_NAME, null, null);
		db.delete(TRACKS_TABLE_NAME, null, null);
		db.delete(XPLEVELS_TABLE_NAME, null, null);
		db.delete(ADD_SPEAKERS_TABLE_NAME, null, null);
	}
	
	/**
	 * Searches the database for the passed conference track. If it does not exist, it adds it.
	 * <br><b>This searches by track title, not id.</b>
	 * @param target	A Track object containing the track to find.
	 * @return	The id of the track.
	 */
	public long getOrAddTrack(Track target) {
		// Searches for passed track. Adds it if not found.
		//	Either way, return the id of the track.
		long trackid = 0;
		Cursor c = null;
		
		try {
			c = this.db.query(TRACKS_TABLE_NAME, 
					new String[] {"id", "tracktitle"}, 
					"tracktitle = ?", 
					new String[] {target.getTrackTitle()}, 
					null, 
					null, 
					null);
			if (c.moveToFirst()) {
				trackid = c.getLong(0);
			} else {
				target.setId(insertTrack(target));
				trackid = target.getId();
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return trackid;
	}
	
	
	/**
	 * Searches the database for the passed speaker. If it does not exist, it adds it.
	 * <br><b>This searches by speaker name, not id.</b>
	 * @param target	A Speaker object containing the speaker to find.
	 * @return	The id of the speaker.
	 */
	public long getOrAddSpeaker(Speaker target) {
		long speakerid = 0;
		Cursor c = null;
		
		try {
			c = this.db.query(SPEAKERS_TABLE_NAME, 
					new String[] {"id","speakername"},
					"speakername = ?",
					new String[] {target.getSpeakerName()}, 
					null,
					null,
					null);
			if (c.moveToFirst()) {
				speakerid = c.getLong(0);
			} else {
				target.setId(insertSpeaker(target));
				speakerid = target.getId();
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return speakerid;
	}
	
	/**
	 * Searches the database for the passed experience level. If it does not exist, it adds it.
	 * <br><b>This searches by level name, not id.</b>
	 * @param target	A ExperienceLevel object containing the level to find.
	 * @return	The id of the experience level.
	 */
	public long getOrAddXPLevel(ExperienceLevel target) {
		long xplevelid = 0;
		Cursor c = null;
		
		try {
			c = this.db.query(XPLEVELS_TABLE_NAME,
					new String[] {"id","levelname"},
					"levelname = ?",
					new String[] {target.getLevelName()},
					null,
					null,
					null);
			if (c.moveToFirst()) {
				xplevelid = c.getLong(0);
			} else {
				target.setId(insertXPLevel(target));
				xplevelid = target.getId();
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return xplevelid;
	}
	
	/**
	 * Database open helper
	 * @author roger
	 *
	 */
	private static class OpenHelper extends SQLiteOpenHelper {
		
		public OpenHelper(Context context) {
			//	context, databasename, CursorFactory, dbversion
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			// Called to create the database
			// Actually by this point the db already exists; we must need to create the tables.
			Log.v(LOG_TAG, "OpenHelper.onCreate");

			ArrayList<StringBuilder> tables = new ArrayList<StringBuilder>();
			ArrayList<StringBuilder> indexes = new ArrayList<StringBuilder>();
			
			tables.add(new StringBuilder()
				.append("create table " + XPLEVELS_TABLE_NAME)
				.append("(id integer primary key,")
				.append("levelname text)"));
			tables.add(new StringBuilder()
				.append("create table " + TRACKS_TABLE_NAME)
				.append("(id integer primary key,")
				.append("tracktitle text)"));
			tables.add(new StringBuilder()
				.append("create table " + SPEAKERS_TABLE_NAME)
				.append("(id integer primary key,")
				.append("speakername text,")
				.append("speakerbio text,")
				.append("twitterhandle text,")
				.append("company text,")
				.append("website text,")
				.append("photourl text)"));
			tables.add(new StringBuilder()
				.append("create table " + SESSIONS_TABLE_NAME)
				.append("(id integer primary key,")
				.append("sessiontitle text,")
				.append("synopsis text,")
				.append("fktrack integer,")
				.append("fkspeaker integer,")
				.append("fkgeneralxplevel integer,")
				.append("fkspecificxplevel integer,")
				.append("award text,")
				.append("technologies text,")
				.append("startdatetime text,")			// sqlite has no datetime datatype
				.append("enddatetime text,")			// these fields hold the time in milliseconds since 1/1/1970 as a string
				.append("room text,")
				.append("voterank text)"));
			tables.add(new StringBuilder()
				.append("create table " + ADD_SPEAKERS_TABLE_NAME)
				.append("(id integer primary key,")
				.append("fksession integer,")
				.append("fkspeaker integer)"));
			//	Don't need to create an index on the primary key; sqlite will do that automatically.
			indexes.add(new StringBuilder()
				.append("create index ix_" + XPLEVELS_TABLE_NAME + "_name on " + XPLEVELS_TABLE_NAME)
				.append("(levelname)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + TRACKS_TABLE_NAME + "_title on " + TRACKS_TABLE_NAME)
				.append("(tracktitle)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + SPEAKERS_TABLE_NAME + "_name on " + SPEAKERS_TABLE_NAME)
				.append("(speakername)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + SESSIONS_TABLE_NAME + "_title on " + SESSIONS_TABLE_NAME)
				.append("(sessiontitle)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + SESSIONS_TABLE_NAME + "_track on " + SESSIONS_TABLE_NAME)
				.append("(fktrack)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + SESSIONS_TABLE_NAME + "_speaker on " + SESSIONS_TABLE_NAME)
				.append("(fkspeaker)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + SESSIONS_TABLE_NAME + "_startdatetime on " + SESSIONS_TABLE_NAME)
				.append("(startdatetime)"));
			indexes.add(new StringBuilder()
				.append("create index ix_" + ADD_SPEAKERS_TABLE_NAME + "_session on " + ADD_SPEAKERS_TABLE_NAME)
				.append("(fksession)"));
			for (StringBuilder step : tables) {
				db.execSQL(step.toString());
			}
			for (StringBuilder step : indexes) {
				db.execSQL(step.toString());
			}
			
		}
		
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.v(LOG_TAG, "OpenHelper.onUpgrade");
			db.execSQL("drop table if exists " + SESSIONS_TABLE_NAME);
			db.execSQL("drop table if exists " + SPEAKERS_TABLE_NAME);
			db.execSQL("drop table if exists " + TRACKS_TABLE_NAME);
			db.execSQL("drop table if exists " + XPLEVELS_TABLE_NAME);
			db.execSQL("drop table if exists " + ADD_SPEAKERS_TABLE_NAME);
			onCreate(db);
			
		}
	}
	
}
