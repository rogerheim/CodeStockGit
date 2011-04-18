/*
 * Copyright 2010-2011 Roger Heim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aremaitch.codestock2010.library;

//  A class to centralize access to preferences.
//  However, the CSPreferenceActivity is writing to some preferences
//  using the caps built-in to PreferenceActivity.

import android.content.Context;
import android.content.SharedPreferences;
import com.aremaitch.utils.NotImplementedException;


//  This is a problem. There is no clean way to get access to named, SharedPreferences without
//  a context. And that means every method that calls this (whether it be activity or service) will
//  need to pass in a context. Or to a ctor.

public class CSPreferenceManager {

    private Context ctx;

    public CSPreferenceManager(Context ctx) {
        this.ctx = ctx;
    }

    private SharedPreferences createSharedPreferences() {
        return ctx.getSharedPreferences(CSConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Returns Twitter update integration user preference.
     *
     * @return <b>True</b> if user has enabled Twitter integration, <b>False</b> otherwise.
     */
    public boolean isTwitterUpdateEnabled() {
        return createSharedPreferences().getBoolean(TwitterConstants.TWITTER_BK_UPD_ENABLED_PREF, false);
    }

    /**
     * Sets Twitter update integration user preference.
     * 
     * @param isEnabled <b>True</b> if integration should be turned on, <b>False</b> otherwise.
     */
    public void setTwitterUpdateEnabled(boolean isEnabled) {
        createSharedPreferences().edit().putBoolean(TwitterConstants.TWITTER_BK_UPD_ENABLED_PREF, isEnabled).commit();
    }

    /**
     * Returns how long to display each tweet.
     *
     * @return Number of seconds to display each tweet.
     */
    public int getTweetDisplayDuration() {
        return Integer.parseInt(createSharedPreferences().getString(TwitterConstants.TWEET_DISPLAY_DURATION_PREF, "10"));
    }

    /**
     * Returns id of last tweet retrieved from Twitter.
     *
     * @return The unique id of the last retrieved tweet.
     */
    public long getLastRetrievedTweetId() {
        return createSharedPreferences().getLong(TwitterConstants.LAST_RETRIEVED_TWEETID_PREF, -1);
    }

    /**
     * Saves id of last tweet retrieved from Twitter.
     *
     * @param tweetId The unique id of the last retrieved tweet.
     */
    public void setLastRetrievedTweetId(long tweetId) {
        createSharedPreferences().edit().putLong(TwitterConstants.LAST_RETRIEVED_TWEETID_PREF, tweetId).commit();
    }

    /**
     * Returns id of last displayed tweet.
     *
     * @return the unique id of the last displayed tweet.
     */
    public long getLastDisplayedTweetId() {
        return createSharedPreferences().getLong(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF, 0);
    }

    /**
     * Saves id of last displayed tweet.
     *
     * @param tweetId The id of the last displayed tweet.
     */
    public void setLastDisplayedTweetId(long tweetId) {
        createSharedPreferences().edit().putLong(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF, tweetId).commit();
    }

    /**
     * Returns the twitter update interval.
     *
     * @return Number of minutes between Twitter updates.
     */
    public int getTwitterUpdateInterval() {
        return Integer.parseInt(createSharedPreferences().getString(TwitterConstants.TWITTER_BK_UPD_INTERVAL_PREF, "5"));
    }

    /**
     * Sets the Twitter update interval.
     * @param interval The interval in minutes between Twitter updates.
     */
    public void setTwitterUpdateInterval(int interval) {
        createSharedPreferences().edit().putString(TwitterConstants.TWITTER_BK_UPD_INTERVAL_PREF, String.valueOf(interval)).commit();
    }

    /**
     * Returns the number of days of tweets to keep.
     *
     * @return Number of days.
     */
    public int getTweetDaysToKeep() {
        return Integer.parseInt(createSharedPreferences().getString(TwitterConstants.TWEET_DAYS_TO_KEEP_PREF, "21"));
    }

    /**
     * Removes the last displayed tweet id.
     *
     * Used when resetting tweet database.
     */
    public void removeLastDisplayedTweetId() {
        createSharedPreferences().edit().remove(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF).commit();
    }

    /**
     * Removes the last retrieved tweet id.
     *
     * Used when resetting tweet database.
     */
    public void removeLastRetrievedTweetId() {
        createSharedPreferences().edit().remove(TwitterConstants.LAST_RETRIEVED_TWEETID_PREF).commit();
    }

    /**
     * Removes both last displayed and last retrieved tweet id in one atomic operation.
     *
     * Used when resetting tweet database.
     */
    public void removeLastTweetId() {
        createSharedPreferences().edit().remove(TwitterConstants.LAST_DISPLAYED_TWEETID_PREF).remove(TwitterConstants.LAST_RETRIEVED_TWEETID_PREF).commit();
    }

    /**
     * Returns the stored Twitter access token.
     *
     * @return A String containing the access token.
     */
    public String getTwitterAccessToken() {
        return createSharedPreferences().getString(TwitterConstants.ACCESS_TOKEN_PREF, "");
    }

    /**
     * Returns the stored Twitter access token secret.
     *
     * @return A String containing the access token secret.
     */
    public String getTwitterAccessTokenSecret() {
        return createSharedPreferences().getString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, "");
    }

    /**
     * Returns the stored Twitter user screen nane.
     *
     * @return A String containing the uesr's screen name.
     */
    public String getTwitterScreenName() {
        return createSharedPreferences().getString(TwitterConstants.ACCESS_TOKEN_SCREENNAME_PREF, "");
    }

    /**
     * Stores the Twitter OAuth access token.
     *
     * @param token The token to store.
     */
    public void setTwitterAccessToken(String token) {
        createSharedPreferences().edit().putString(TwitterConstants.ACCESS_TOKEN_PREF, token).commit();
    }

    /**
     * Stores the Twitter OAuth access secret.
     *
     * @param secret The secret to store.
     */
    public void setTwitterAccessTokenSecret(String secret) {
        createSharedPreferences().edit().putString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, secret).commit();
    }

    /**
     * Stores the user's Twitter screen name.
     *
     * @param screenName The screen name to store.
     */
    public void setTwitterScreenName(String screenName) {
        createSharedPreferences().edit().putString(TwitterConstants.ACCESS_TOKEN_SCREENNAME_PREF, screenName).commit();
    }

    /**
     * Saves the Twitter OAuth tokens, token secret, and user screen name in one atomic operation.
     *
     * @param accessToken A String containing the access token.
     * @param tokenSecret A String containing the access token secret.
     * @param screenName A String containing the user's screen name.
     */
    public void setTwitterOAuthData(String accessToken, String tokenSecret, String screenName) {
        createSharedPreferences().edit()
            .putString(TwitterConstants.ACCESS_TOKEN_PREF, accessToken)
            .putString(TwitterConstants.ACCESS_TOKEN_SECRET_PREF, tokenSecret)
            .putString(TwitterConstants.ACCESS_TOKEN_SCREENNAME_PREF, screenName)
            .commit();
    }

    /**
     * Removes the Twitter OAuth tokens in one atomic operation.
     */
    public void removeTwitterOAuthData() {
        createSharedPreferences().edit()
            .remove(TwitterConstants.ACCESS_TOKEN_PREF)
            .remove(TwitterConstants.ACCESS_TOKEN_SECRET_PREF)
            .remove(TwitterConstants.ACCESS_TOKEN_SCREENNAME_PREF)
            .commit();
    }

    /**
     * Returns the user's schedule builder id from the CodeStock website.
     *
     * @return A long containing the user's schedule builder id or zero if the id has not yet been set.
     */
    public long getScheduleUserId() {
        return createSharedPreferences().getLong(CSConstants.SCHEDULE_BUILDER_USERID_PREF, 0);
    }

    /**
     * Saves the user's schedule builder id from the CodeStock website.
     *
     * @param userId A long containing the user's schedule builder id.
     */
    public void setScheduleUserId(long userId) {
        createSharedPreferences().edit().putLong(CSConstants.SCHEDULE_BUILDER_USERID_PREF, userId).commit();
    }

    public int getFlingDistanceSensitivity() {
        return Integer.parseInt(createSharedPreferences().getString(CSConstants.NAVIGATION_FLING_DISTANCE, "2"));
    }

    public int getFlingVelocityThreshold() {
        return Integer.parseInt(createSharedPreferences().getString(CSConstants.NAVIGATION_FLING_SPEED, "1000"));
    }
}
