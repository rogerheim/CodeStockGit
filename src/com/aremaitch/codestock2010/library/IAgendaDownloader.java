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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * Date: 2/10/11
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IAgendaDownloader {
    JSONObject getSpeakerData() throws JSONException;
    JSONObject getSessionData() throws JSONException;
}
