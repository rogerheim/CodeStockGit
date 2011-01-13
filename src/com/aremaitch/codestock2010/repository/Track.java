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

package com.aremaitch.codestock2010.repository;

import java.util.List;


public class Track {
	private long id;
	private String trackTitle;
	private List<Session> sessions;
	private List<MiniSession> miniSessions;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTrackTitle() {
		return trackTitle;
	}
	public void setTrackTitle(String trackTitle) {
		this.trackTitle = trackTitle;
	}
	public List<Session> getSessions() {
		return this.sessions;
	}
	public void setSessions(List<Session> _sessions) {
		this.sessions = _sessions;
	}
	public List<MiniSession> getMiniSessions() {
		return this.miniSessions;
	}
	public void setMiniSessions(List<MiniSession> _miniSessions) {
		this.miniSessions = _miniSessions;
	}
}
