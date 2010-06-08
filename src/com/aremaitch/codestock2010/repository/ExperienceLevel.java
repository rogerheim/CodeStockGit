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

public class ExperienceLevel {
	private long id;
	private String levelName;
	private List<Session> generalSessions;
	private List<Session> specificSessions;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getLevelName() {
		return levelName;
	}
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	public List<Session> getGeneralSessions() {
		return this.generalSessions;
	}
	public void setGeneralSessions(List<Session> _generalSessions) {
		this.generalSessions = _generalSessions;
	}
	public List<Session> getSpecificSessions() {
		return this.specificSessions;
	}
	public void setSpecificSessions(List<Session> _specificSessions) {
		this.specificSessions = _specificSessions;
	}
	
}
