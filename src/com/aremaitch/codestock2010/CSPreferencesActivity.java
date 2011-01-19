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

package com.aremaitch.codestock2010;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.text.TextUtils;
import com.aremaitch.codestock2010.library.TwitterConstants;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/13/11
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSPreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, CSPreferencesActivity.class);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addPreferencesFromResource(R.xml.prefs);
        initializePreferenceScreen();


    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //TODO: Implement reacting to preference changes

        //  If twitter is enabled:
        //      If we don't already have an access token, prompt the user to begin oauth. If they decline, disable twitter.
        //      If we already have an access token, do nothing.
        //  If twitter is disabled:
        //      If we already have an access token, ask the user if then want to forget the cached authentication info.
        //      If the user says to forget the auth info, delete the access token and access token secret preferences.
        //      If the user says to not forget the auth info, do nothing.

    }


    private boolean isAlreadyAuthenticated(SharedPreferences prefs) {
        return !(TextUtils.isEmpty(prefs.getString(TwitterConstants.ACCESS_TOKEN_PREF, ""))
                || TextUtils.isEmpty(prefs.getString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, "")));
    }

    private boolean doesUserWantToAuthenticate() {
        final boolean[] result = {false};
        new AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("You've not authorized the CodeStock app to access your Twitter account yet.\n\nIf you press Yes you'll be " +
                        "taken to a Twitter page where you'll be able to login (if you're not already logged in on this device) " +
                        "and grant this app access. Your password is not stored on this device.\n\nDo you want to continue?")
            .setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    result[0] = true;
                }
            })
            .setTitle("Authenticate to Twitter")
            .show();

        return result[0];
    }

    //  Create this in code so I can use constants in TwitterConstants instead of embedding them
    //  in an XML file.

    private void beginOAuthDance() {

    }

    //TODO: Build a builder class for more fluent building of these preferences.

    //  Need to work around an Android bug (IMO.) If you call setDependency() Android will call
    //  PreferenceManager.findPreference() to find the dependency. But if you haven't yet called
    //  setPreferenceScreen() (because you're building up the entire hierarchy first) the call will
    //  fail with an IllegalStateException. Must setPreferenceScreen() before setDependency().

    private void initializePreferenceScreen() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        root.setTitle("CodeStock Preferences");


        PreferenceCategory twitterCat = new PreferenceCategory(this);
        twitterCat.setTitle("Twitter");
        root.addPreference(twitterCat);

        CheckBoxPreference twitterEnabled = new CheckBoxPreference(this);
        twitterEnabled.setKey(TwitterConstants.TWITTER_ENABLED);
        twitterEnabled.setDefaultValue(false);
        twitterEnabled.setTitle("Enable Twitter");
        twitterEnabled.setSummary("Enable Twitter support");
        twitterCat.addPreference(twitterEnabled);

        CheckBoxPreference bkUpdEnabled = new CheckBoxPreference(this);
        bkUpdEnabled.setKey(TwitterConstants.TWITTER_BK_UPD_ENABLED_PREF);
        bkUpdEnabled.setDefaultValue(false);
        bkUpdEnabled.setTitle("Update Tweets in background");
        bkUpdEnabled.setSummary("This will run your battery down faster.");
        twitterCat.addPreference(bkUpdEnabled);

        ListPreference bkUpdInterval = new ListPreference(this);
        bkUpdInterval.setKey(TwitterConstants.TWITTER_BK_UPD_INTERVAL_PREF);
        bkUpdInterval.setDefaultValue("5");
        bkUpdInterval.setTitle("Minutes between updates");
        bkUpdInterval.setSummary("15, 30, & 60 are more efficient");
        bkUpdInterval.setEntries(R.array.twitter_bk_upd_interval_entries);
        bkUpdInterval.setEntryValues(R.array.twitter_bk_upd_interval_entryvalues);
        bkUpdInterval.setDialogTitle("Background Update Interval");
        twitterCat.addPreference(bkUpdInterval);

        ListPreference tweetDisplayDuration = new ListPreference(this);
        tweetDisplayDuration.setKey(TwitterConstants.TWEET_DISPLAY_DURATION_PREF);
        tweetDisplayDuration.setDefaultValue("5");
        tweetDisplayDuration.setTitle("Tweet display seconds");
        tweetDisplayDuration.setSummary("Seconds to display each tweet");
        tweetDisplayDuration.setEntries(R.array.tweet_dsply_duration_entries);
        tweetDisplayDuration.setEntryValues(R.array.tweet_dsply_duration_entryvalues);
        tweetDisplayDuration.setDialogTitle("Tweet Display Seconds");
        twitterCat.addPreference(tweetDisplayDuration);

        ListPreference tweetDaysToKeep = new ListPreference(this);
        tweetDaysToKeep.setKey(TwitterConstants.TWEET_DAYS_TO_KEEP_PREF);
        tweetDaysToKeep.setDefaultValue("21");
        tweetDaysToKeep.setTitle("Tweet Days to Keep");
        tweetDaysToKeep.setSummary("How many days of tweets do you want to keep?");
        tweetDaysToKeep.setEntries(R.array.tweet_db_daystokeep_entries);
        tweetDaysToKeep.setEntryValues(R.array.tweet_db_daystokeep_entryvalues);
        tweetDaysToKeep.setDialogTitle("Days of Tweets to Keep");
        twitterCat.addPreference(tweetDaysToKeep);


        PreferenceCategory aboutCat = new PreferenceCategory(this);
        aboutCat.setTitle("About");
        root.addPreference(aboutCat);

        PreferenceScreen aboutPref = getPreferenceManager().createPreferenceScreen(this);
        aboutPref.setTitle("About CodeStock...");
        aboutPref.setIntent(new Intent(this, AboutActivity.class));
        aboutCat.addPreference(aboutPref);

        setPreferenceScreen(root);

        bkUpdEnabled.setDependency(TwitterConstants.TWITTER_ENABLED);
        bkUpdInterval.setDependency(TwitterConstants.TWITTER_ENABLED);
        tweetDisplayDuration.setDependency(TwitterConstants.TWITTER_ENABLED);
        tweetDaysToKeep.setDependency(TwitterConstants.TWITTER_ENABLED);

    }
}
