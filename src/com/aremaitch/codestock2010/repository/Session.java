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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//import org.joda.time.DateTime;


public class Session {
	private long id;
	private String sessionTitle;
	private String synopsis;
	private Track track;
	private Speaker speaker;
	private ExperienceLevel generalExperienceLevel;
	private ExperienceLevel specificExperienceLevel;
	private String award;
	private String technologies;
	private Calendar startDate;
	private Calendar endDate;
	private String room;
	private List<Speaker> additionalSpeakers;
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSessionTitle() {
		return sessionTitle;
	}
	public void setSessionTitle(String sessionTitle) {
		this.sessionTitle = sessionTitle;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}
	public Track getTrack() {
		if (track == null) {
			loadTrack();
		}
		return track;
	}
	public void setTrack(Track track) {
		this.track = track;
	}
	
	private void loadTrack() {
		
	}
	public Speaker getSpeaker() {
		return speaker;
	}
	public void setSpeaker(Speaker speaker) {
		this.speaker = speaker;
	}
	public ExperienceLevel getGeneralExperienceLevel() {
		return generalExperienceLevel;
	}
	public void setGeneralExperienceLevel(ExperienceLevel generalExperienceLevel) {
		this.generalExperienceLevel = generalExperienceLevel;
	}
	public ExperienceLevel getSpecificExperienceLevel() {
		return specificExperienceLevel;
	}
	public void setSpecificExperienceLevel(ExperienceLevel specificExperienceLevel) {
		this.specificExperienceLevel = specificExperienceLevel;
	}
	public String getAward() {
		return award;
	}
	public void setAward(String award) {
		this.award = award;
	}
	public String getTechnologies() {
		return technologies;
	}
	public void setTechnologies(String technologies) {
		this.technologies = technologies;
	}
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}
	public Calendar getStartDate() {
		return startDate;
	}
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
	public Calendar getEndDate() {
		return endDate;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public String getRoom() {
		return room;
	}
	public void setAdditionalSpeakers(List<Speaker> additionalSpeakers) {
		this.additionalSpeakers = additionalSpeakers;
	}
	public List<Speaker> getAdditionalSpeakers() {
		if (this.additionalSpeakers == null) {
			this.additionalSpeakers = new ArrayList<Speaker>();
		}
		return this.additionalSpeakers;
	}
	public void addAdditionalSpeaker(Speaker additionalSpeaker) {
		if (this.additionalSpeakers == null) {
			this.additionalSpeakers = new ArrayList<Speaker>();
		}
		this.additionalSpeakers.add(additionalSpeaker);
	}
}
