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

import java.util.List;

public class Speaker {
	private long id;
	private String speakerName;
	private String speakerBio;
	private String twitterHandle;
	private String company;
	private String webSite;
	private String speakerPhotoUrl;
	private List<Session> sessions;
	private List<Session> sessionsWhereAdditionalSpeaker;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSpeakerName() {
		return speakerName;
	}
	public void setSpeakerName(String speakerName) {
		this.speakerName = speakerName;
	}
	public String getSpeakerBio() {
		return speakerBio;
	}
	public void setSpeakerBio(String speakerBio) {
		this.speakerBio = speakerBio;
	}
	public String getTwitterHandle() {
		return twitterHandle;
	}
	public void setTwitterHandle(String twitterHandle) {
		this.twitterHandle = twitterHandle;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getWebSite() {
		return webSite;
	}
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	public void setSpeakerPhotoUrl(String speakerPhotoUrl) {
		this.speakerPhotoUrl = speakerPhotoUrl;
	}
	public String getSpeakerPhotoUrl() {
		return speakerPhotoUrl;
	}
	public List<Session> getSessions() {
		return sessions;
	}
	public void setSessions(List<Session> _sessions) {
		this.sessions = _sessions;
	}
	public List<Session> getSessionsWhereAdditionalSpeaker() {
		return sessionsWhereAdditionalSpeaker;
	}
	public void setSessionsWhereAdditionalSpeaker(
			List<Session> sessionsWhereAdditionalSpeaker) {
		this.sessionsWhereAdditionalSpeaker = sessionsWhereAdditionalSpeaker;
	}
	
}
