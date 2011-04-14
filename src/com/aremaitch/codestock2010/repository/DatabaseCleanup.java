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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.aremaitch.codestock2010.library.CSConstants;
import com.aremaitch.codestock2010.library.CSPreferenceManager;
import com.aremaitch.codestock2010.library.TwitterConstants;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseCleanup {
    private Context _ctx;
    private DataHelper _dh;

    public DatabaseCleanup(Context _ctx) {
        this._ctx = _ctx;
    }

    public void run() {
        _dh = new DataHelper(_ctx);

        //  Delete tweets that have been deleted by the original user
        _dh.cleanUpDeletedTweets();

        //  Clean up old tweets based on user's preference
        _dh.cleanUpOldTweets(new CSPreferenceManager(_ctx).getTweetDaysToKeep());
        _dh.close();
    }
}
