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
import android.util.AttributeSet;
import com.aremaitch.codestock2010.library.*;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.utils.ACLogger;
import com.aremaitch.utils.Command;
import com.aremaitch.utils.OnClickCommandWrapper;
import com.flurry.android.FlurryAgent;

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
        ACLogger.info(CSConstants.LOG_TAG, "CSPreferencesActivity onCreate");
        super.onCreate(savedInstanceState);
        cancelBackgroundServices();
        //addPreferencesFromResource(R.xml.prefs);
        initializePreferenceScreen();


    }

    @Override
    protected void onResume() {
        ACLogger.info(CSConstants.LOG_TAG, "CSPreferencesActivity onResume");
        super.onResume();
        //PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (!isAlreadyAuthenticated(getPreferenceManager().getSharedPreferences())) {
//            cancelBackgroundServices();
            forceTwitterDisabled();
        }
    }

    @Override
    protected void onPause() {
        ACLogger.info(CSConstants.LOG_TAG, "CSPreferencesActivity onPause");
        super.onPause();
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        possiblyStartBackgroundServices();
    }

    private void forceTwitterDisabled() {
        ((CheckBoxPreference)getPreferenceScreen().findPreference(TwitterConstants.TWITTER_ENABLED_PREF))
                .setChecked(false);
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


        if (key.equalsIgnoreCase(TwitterConstants.TWITTER_ENABLED_PREF)) {
            if (sharedPreferences.getBoolean(key, false)) {
                //  Twitter was enabled; if not already authenticated, ask user if they want to authenticate.
                if (!isAlreadyAuthenticated(sharedPreferences)) {
                    doesUserWantToAuthenticate();
                } else {
                    //  Twitter is enabled and we already had oauth tokens. Just log it.
                    AnalyticsManager.logEvent(CSPreferencesActivity.this, FlurryEvent.TWITTER_INTEG_RESUMED);
                }
            } else {
                //  Twitter was disabled; if already authenticated, ask user if they want to delete tokens.
                if (isAlreadyAuthenticated(sharedPreferences)) {
                    doesUserWantToForgetOAuthToken();
                }
            }
        }
    }

    private void doesUserWantToForgetOAuthToken() {

        Command yesCommand = new Command() {
            @Override
            public void execute() {
                new TwitterOAuth().forgetOAuthTokens(CSPreferencesActivity.this);
            }
        };

        Command noCommand = new Command() {
            @Override
            public void execute() {
                AnalyticsManager.logEvent(CSPreferencesActivity.this, FlurryEvent.TWITTER_INTEG_PAUSED);
            }
        };


        new AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(getString(R.string.pref_query_delete_twitter_text))
            .setNegativeButton(getString(R.string.no_string), new OnClickCommandWrapper(noCommand))
            .setPositiveButton(getString(R.string.yes_string), new OnClickCommandWrapper(yesCommand))
            .setTitle(getString(R.string.pref_query_delete_twitter_title_text))
            .show();
    }


    private void possiblyStartBackgroundServices() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        if (prefs.getBoolean(TwitterConstants.TWITTER_ENABLED_PREF, false)) {
            if (prefs.getBoolean(TwitterConstants.TWITTER_BK_UPD_ENABLED_PREF, false)) {
                BackgroundTaskManager btm = new BackgroundTaskManager(this);
                btm.setRecurringTweetScan();
                btm.setDBCleanupTask();
            }
        }
    }

    private void cancelBackgroundServices() {
        BackgroundTaskManager btm = new BackgroundTaskManager(this);
        btm.cancelRecurringTweetScan();
        btm.cancelDBCleanupTask();
    }

    private boolean isAlreadyAuthenticated(SharedPreferences prefs) {
        return !(TextUtils.isEmpty(prefs.getString(TwitterConstants.ACCESS_TOKEN_PREF, ""))
                || TextUtils.isEmpty(prefs.getString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, "")));
    }

    //  AlertDialogs do not block and wait for a response.
    private void doesUserWantToAuthenticate() {
        Command yesCommand = new Command() {
            @Override
            public void execute() {
                beginOAuthDance();
            }
        };

        Command noCommand = new Command() {
            @Override
            public void execute() {
                forceTwitterDisabled();
            }
        };

        new AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(getString(R.string.pref_query_authenticate_to_twitter_text))
            .setNegativeButton(getString(R.string.no_string), new OnClickCommandWrapper(noCommand))
            .setPositiveButton(getString(R.string.yes_string), new OnClickCommandWrapper(yesCommand))
            .setTitle(getString(R.string.pref_query_authenticate_to_twitter_title_text))
            .show();
    }


    private void beginOAuthDance() {
        OAuthActivity.startMe(this);
    }

    //TODO: Build a builder class for more fluent building of these preferences.

    //  Need to work around an Android bug (IMO.) If you call setDependency() Android will call
    //  PreferenceManager.findPreference() to find the dependency. But if you haven't yet called
    //  setPreferenceScreen() (because you're building up the entire hierarchy first) the call will
    //  fail with an IllegalStateException. Must setPreferenceScreen() before setDependency().

    //  Create this in code so I can use constants in TwitterConstants instead of embedding them
    //  in an XML file.
    private void initializePreferenceScreen() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        root.getPreferenceManager().setSharedPreferencesName(CSConstants.SHARED_PREF_NAME);
        root.setTitle(R.string.pref_root_title);

        /*
         *      Twitter category
         */
        PreferenceCategory twitterCat = new PreferenceCategory(this);
        twitterCat.setTitle(R.string.pref_twitter_cat_title);
        root.addPreference(twitterCat);

        CheckBoxPreference twitterEnabled = new CheckBoxPreference(this);
        twitterEnabled.setKey(TwitterConstants.TWITTER_ENABLED_PREF);
        twitterEnabled.setDefaultValue(false);
        twitterEnabled.setTitle(R.string.pref_twitter_enabled_title);
        twitterEnabled.setSummaryOn(R.string.pref_twitter_summary_on);
        twitterEnabled.setSummaryOff(R.string.pref_twitter_summary_off);
        twitterCat.addPreference(twitterEnabled);

        CheckBoxPreference bkUpdEnabled = new CheckBoxPreference(this);
        bkUpdEnabled.setKey(TwitterConstants.TWITTER_BK_UPD_ENABLED_PREF);
        bkUpdEnabled.setDefaultValue(false);
        bkUpdEnabled.setTitle(R.string.pref_twitter_background_title);
        bkUpdEnabled.setSummaryOn(R.string.pref_twitter_background_summary_on);
        bkUpdEnabled.setSummaryOff(R.string.pref_twitter_background_summary_off);
        twitterCat.addPreference(bkUpdEnabled);

        ListPreference bkUpdInterval = new ListPreference(this);
        bkUpdInterval.setKey(TwitterConstants.TWITTER_BK_UPD_INTERVAL_PREF);
        bkUpdInterval.setDefaultValue("5");
        bkUpdInterval.setTitle(R.string.pref_twitter_upd_int_title);
        bkUpdInterval.setSummary(R.string.pref_twitter_upd_int_summary);
        bkUpdInterval.setEntries(R.array.twitter_bk_upd_interval_entries);
        bkUpdInterval.setEntryValues(R.array.twitter_bk_upd_interval_entryvalues);
        bkUpdInterval.setDialogTitle(R.string.pref_twitter_upd_int_dlg_title);
        twitterCat.addPreference(bkUpdInterval);

        ListPreference tweetDisplayDuration = new ListPreference(this);
        tweetDisplayDuration.setKey(TwitterConstants.TWEET_DISPLAY_DURATION_PREF);
        tweetDisplayDuration.setDefaultValue("10");
        tweetDisplayDuration.setTitle(R.string.pref_twitter_display_title);
        tweetDisplayDuration.setSummary(R.string.pref_twitter_display_summary);
        tweetDisplayDuration.setEntries(R.array.tweet_dsply_duration_entries);
        tweetDisplayDuration.setEntryValues(R.array.tweet_dsply_duration_entryvalues);
        tweetDisplayDuration.setDialogTitle(R.string.pref_twitter_display_dlg_title);
        twitterCat.addPreference(tweetDisplayDuration);

        ListPreference tweetDaysToKeep = new ListPreference(this);
        tweetDaysToKeep.setKey(TwitterConstants.TWEET_DAYS_TO_KEEP_PREF);
        tweetDaysToKeep.setDefaultValue("21");
        tweetDaysToKeep.setTitle(R.string.pref_twitter_keep_title);
        tweetDaysToKeep.setSummary(R.string.pref_twitter_keep_summary);
        tweetDaysToKeep.setEntries(R.array.tweet_db_daystokeep_entries);
        tweetDaysToKeep.setEntryValues(R.array.tweet_db_daystokeep_entryvalues);
        tweetDaysToKeep.setDialogTitle(R.string.pref_twitter_keep_dlg_title);
        twitterCat.addPreference(tweetDaysToKeep);

        /*
         *      Navigation category
         */

        PreferenceCategory navCat = new PreferenceCategory(this);
        navCat.setTitle(R.string.pref_nav_cat_title);
        root.addPreference(navCat);

        CheckBoxPreference startAgendaBasedOnDateTime = new CheckBoxPreference(this);
        startAgendaBasedOnDateTime.setKey(CSConstants.START_AGENDA_BASEDON_DATETIME_PREF);
        startAgendaBasedOnDateTime.setDefaultValue(true);
        startAgendaBasedOnDateTime.setTitle(R.string.pref_nav_agenda_title);
        startAgendaBasedOnDateTime.setSummaryOn(R.string.pref_nav_agenda_summary_on);
        startAgendaBasedOnDateTime.setSummaryOff(R.string.pref_nav_agenda_summary_off);
        navCat.addPreference(startAgendaBasedOnDateTime);

        /*
         *      Analytics opt-out
         */
        PreferenceCategory analyticsCat = new PreferenceCategory(this);
        analyticsCat.setTitle(R.string.pref_analytics_cat_title);
        root.addPreference(analyticsCat);
        PreferenceScreen analyticsPref = getPreferenceManager().createPreferenceScreen(this);
        analyticsPref.setTitle(R.string.pref_analytics_opt_title);
        analyticsPref.setIntent(new Intent(this, AnalyticsOptOutActivity.class));
        analyticsCat.addPreference(analyticsPref);


        /*
         *      Tools category
         */
        PreferenceCategory toolsCat = new PreferenceCategory(this);
        toolsCat.setTitle(R.string.pref_tools_cat_title);
        root.addPreference(toolsCat);

        ResetTweetsDialog resetTweets = new ResetTweetsDialog(this, null);
        resetTweets.setTitle(R.string.pref_tools_reset_title);
        resetTweets.setSummary(R.string.pref_tools_reset_summary);
        resetTweets.setDialogTitle(R.string.pref_tools_reset_dlg_title);
        resetTweets.setDialogMessage(R.string.pref_tools_reset_dlg_msg);
        resetTweets.setPositiveButtonText(getString(R.string.yes_string));
        resetTweets.setNegativeButtonText(getString(R.string.no_string));
        toolsCat.addPreference(resetTweets);


        /*
         *      About category
         */
        PreferenceCategory aboutCat = new PreferenceCategory(this);
        aboutCat.setTitle(R.string.pref_about_cat_title);
        root.addPreference(aboutCat);

        PreferenceScreen aboutPref = getPreferenceManager().createPreferenceScreen(this);
        aboutPref.setTitle(R.string.pref_about_about_title);
        aboutPref.setIntent(new Intent(this, AboutActivity.class));
        aboutCat.addPreference(aboutPref);

        setPreferenceScreen(root);

        bkUpdEnabled.setDependency(TwitterConstants.TWITTER_ENABLED_PREF);
        bkUpdInterval.setDependency(TwitterConstants.TWITTER_ENABLED_PREF);
        tweetDisplayDuration.setDependency(TwitterConstants.TWITTER_ENABLED_PREF);
        tweetDaysToKeep.setDependency(TwitterConstants.TWITTER_ENABLED_PREF);

    }

    class ResetTweetsDialog extends DialogPreference {

        Context ctx;
        public ResetTweetsDialog(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            ctx = context;
        }

        public ResetTweetsDialog(Context context, AttributeSet attrs) {
            super(context, attrs);
            ctx = context;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {

                TwitterAvatarManager tam = new TwitterAvatarManager(ctx);
                tam.nukeAllAvatars();

                DataHelper dh = new DataHelper(ctx);
                dh.dropAllTwitterData();
                dh.close();

                SharedPreferences.Editor ed = getPreferenceManager().getSharedPreferences().edit();
                ed.remove(TwitterConstants.LAST_RETRIEVED_TWEETID_PREF);
                ed.remove(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF);
                ed.commit();

            }
            dialog.dismiss();
        }
    }
}
