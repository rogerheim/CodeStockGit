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

/**
 * Constants for Flurry analytics
 */
public class FlurryEvent {
    private FlurryEvent() {
    }

    //  Note: None of these Twitter event analytics track the actual user id/password/etc.
    //  I'm only interested in tracking how many people are using the Twitter integration
    //      as a percentage of the total number of users.
    /**
     * Track when the user enables Twitter integration.
     */
    public static final String TWITTER_INTEG_ON = "twitter_integ_on";

    /**
     * Track when the user disables Twitter integration and forgets the OAuth tokens.
     */
    public static final String TWITTER_INTEG_OFF = "twitter_integ_off";

    /**
     * Track when the user disables Twitter integration but keeps the OAuth tokens.
     */
    public static final String TWITTER_INTEG_PAUSED = "twitter_integ_paused";

    /**
     * Track when the user enables Twitter integration and uses already saved OAuth tokens.
     */
    public static final String TWITTER_INTEG_RESUMED = "twitter_integ_resumed";

    public static final String SESSION_PAGE_VIEWED = "session_page_viewed";

    public static final String TWITTER_SVC_START = "twitter_svc_start";
    public static final String TWITTER_SVC_STOP = "twitter_svc_stop";
    public static final String TWITTER_SVC_STOP_NO_BKGND = "twitter_svc_stop_no_bkgnd";
    public static final String TWITTER_SVC_STOP_NO_NET = "twitter_svc_stop_no_net";

    public static final String DB_CLEANUP_START = "db_cleanup_start";
    public static final String DB_CLEANUP_STOP = "db_cleanup_stop";

    public static final String AGENDA_DL_START = "agenda_dl_start";
    public static final String AGENDA_DL_STOP = "agenda_dl_stop";
    public static final String AGENDA_DL_FAILED = "agenda_dl_failed";

    public static final String IMGOING_TWEET_SENT = "imgoing_tweet_sent";
    public static final String IMGOING_TWEET_FAILED = "imgoing_tweet_failed";
    public static final String IMHERE_TWEET_SENT = "imhere_tweet_sent";
    public static final String IMHERE_TWEET_FAILED = "imhere_tweet_failed";
    public static final String FEEDBACK_DM_SENT = "feedback_dm_sent";
    public static final String FEEDBACK_DM_FAILED = "feedback_dm_failed";
    

}
