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

import java.util.Calendar;

//import org.joda.time.DateTime;

/**
 * Smaller session object when all you want is the id and session title.
 * 
 * @author Roger Heim, Aremaitch Consulting
 *
 */
public class MiniSession {
	private long _id;
	private String _sessionTitle;
	private Calendar _startDateTime;
	private String _room;
	private String _speakerName;
	private String _award;
	
	public long getId() {
		return _id;
	}
	public void setId(long id) {
		_id = id;
	}
	public String getSessionTitle() {
		return _sessionTitle;
	}
	public void setSessionTitle(String sessionTitle) {
		_sessionTitle = sessionTitle;
	}
	
	public Calendar getStartDateTime() {
		return _startDateTime;
	}
	public void setStartDateTime(Calendar startDateTime) {
		_startDateTime = startDateTime;
	}
	
	public String getRoom() {
		return _room;
	}
	public void setRoom(String room) {
		_room = room;
	}
	
	public String getSpeakerName() {
		return _speakerName;
	}
	public void setSpeakerName(String speakerName) {
		_speakerName = speakerName;
	}
	
	public String getAward() {
		return _award;
	}
	public void setAward(String award) {
		_award = award;
	}
}
