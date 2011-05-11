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

import android.content.Context;
import com.aremaitch.codestock2010.R;
import com.aremaitch.utils.ACLogger;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsManager {

    public static void logStartSession(Context ctx) {
        if (new CSPreferenceManager(ctx).isParticipatingInAnalytics()) {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics logging start session");
            FlurryAgent.onStartSession(ctx, ctx.getString(R.string.flurry_analytics_api_key));
//        } else {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics skipping start session");
        }
    }

    public static void logEndSession(Context ctx) {
        if (new CSPreferenceManager(ctx).isParticipatingInAnalytics()) {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics logging end session");
            FlurryAgent.onEndSession(ctx);
//        } else {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics skipping end session");
        }
    }

    public static void logEvent(Context ctx, String event) {
        if (new CSPreferenceManager(ctx).isParticipatingInAnalytics()) {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics logging event " + event);
            FlurryAgent.logEvent(event);
//        } else {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics skipping event " + event);
        }
    }

    public static void logEvent(Context ctx, String event, Map<String, String> parameters) {
        if (new CSPreferenceManager(ctx).isParticipatingInAnalytics()) {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics logging event " + event);
            FlurryAgent.logEvent(event, parameters);
//        } else {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics skipping event " + event);
        }
    }

    public static void logSessionPageViewed(Context ctx, long sessionid) {
        if (new CSPreferenceManager(ctx).isParticipatingInAnalytics()) {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics logging event " + FlurryEvent.SESSION_PAGE_VIEWED);
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("session_id", String.valueOf(sessionid));

            FlurryAgent.logEvent(FlurryEvent.SESSION_PAGE_VIEWED, parameters);
//        } else {
//            ACLogger.debug(CSConstants.LOG_TAG, "analytics skipping event " + FlurryEvent.SESSION_PAGE_VIEWED);
        }
    }

}
