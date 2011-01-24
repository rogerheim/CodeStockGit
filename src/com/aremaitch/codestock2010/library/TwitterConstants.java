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

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterConstants {
    public static final String OAUTH_CALLBACK_URL = "androidauth2://devcallback";
    public static final String ACCESS_TOKEN_PREF = "twitter_access_token";
    public static final String ACCESS_TOKEN_SECRET_PREF = "twitter_access_token_secret";
    public static final String ACCESS_TOKEN_SCREENNAME_PREF = "twitter_access_token_screenname";
    
    public static final String TWEET_SCAN_HASHTAG_EXTRA_KEY = "hashtags" ;
    public static final String[] SEARCH_HASHTAGS = new String[] {"#codestock","#codestock2011","codestock"};
    public static final String DB_CLEANUP_EXTRA_KEY = "cleanup";
    
    public static final String TWITTER_BK_UPD_ENABLED_PREF = "twitter_bk_upd_enabled";
    public static final String TWITTER_BK_UPD_INTERVAL_PREF = "twitter_bk_upd_interval";
    public static final String TWEET_DISPLAY_DURATION_PREF = "tweet_display_duration";
    public static final String TWEET_DAYS_TO_KEEP_PREF = "tweet_days_to_keep";

    public static final String LAST_TWEETID_PREF = "last_search_tweetid";
    public static final int CODESTOCK_USERID = 14499765;
    public static final String TWITTER_ENABLED = "twitter_enabled";
}
