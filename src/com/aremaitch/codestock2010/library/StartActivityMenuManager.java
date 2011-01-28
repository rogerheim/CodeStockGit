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

import android.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import com.aremaitch.codestock2010.CSPreferencesActivity;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/13/11
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class StartActivityMenuManager {
    private static final String START_OPTIONS_STRING = "Options";
    private static final int START_OPTIONS_ICON = android.R.drawable.ic_menu_preferences;
    private static final int START_MENU_OPTIONS = Menu.FIRST;
    private static final int START_MENU_CLEAR_LAST_RCVD_TWEET = START_MENU_OPTIONS + 1;

    private Context _ctx;

    public StartActivityMenuManager(Context _ctx) {
        this._ctx = _ctx;
    }

    //TODO: Refactor to use command pattern instead of having the menu selection code here. Need wrapper.
    public boolean createStartActivityOptionsMenu(Menu menu) {
        menu.add(0, START_MENU_OPTIONS, 0, START_OPTIONS_STRING).setIcon(START_OPTIONS_ICON);
        menu.add(0, START_MENU_CLEAR_LAST_RCVD_TWEET, 0, "Reset Last Tweet");
        return true;
    }

    public boolean startActivityOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case START_MENU_OPTIONS:
                CSPreferencesActivity.startMe(_ctx);
                return true;

            case START_MENU_CLEAR_LAST_RCVD_TWEET:
                SharedPreferences.Editor ed = _ctx.getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
                ed.remove(TwitterConstants.LAST_TWEETID_PREF);
                ed.commit();
        }
        return false;


    }

}
