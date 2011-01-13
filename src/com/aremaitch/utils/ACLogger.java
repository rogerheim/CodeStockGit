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

package com.aremaitch.utils;

import android.os.Debug;
import android.util.Log;

/**
 * Wrapper around Android Log class
 * @author Roger Heim
 * @since 16-Jun-2010
 *
 * Note: DEBUG, WARN, and VERBOSE messages are filtered out if no debugger is connected.
 *       This is in addition to if a system property was set to control logging.
 *       This property is done with 'setprop' (or pre-set by the carrier) which requires
 *       access to the phone's command line (beyond most users.)
 *       
 *       Debug.isDebuggerConnected() is in android/os/Debug.java and calls down into 
 *       dalvik.system.VMDebug which calls down into native code.
 */
public class ACLogger {
	public static void debug(String tag, String msg) {
		if (Log.isLoggable(tag, Log.DEBUG) && Debug.isDebuggerConnected()) {
			Log.d(tag, msg);
		}
	}
	
	public static void debug(String tag, String msg, Throwable t) {
		if (Log.isLoggable(tag, Log.DEBUG) && Debug.isDebuggerConnected()) {
			Log.d(tag, msg, t);
			Log.d(tag, Log.getStackTraceString(t));
		}
	}
	
	public static void info(String tag, String msg) {
		if (Log.isLoggable(tag, Log.INFO)) {
			Log.i(tag, msg);
		}
	}
	
	public static void error(String tag, String msg) {
		if (Log.isLoggable(tag, Log.ERROR)) {
			Log.e(tag, msg);
		}
	}
	
	public static void error(String tag, String msg, Throwable t) {
		if (Log.isLoggable(tag, Log.ERROR)) {
			Log.e(tag, msg, t);
			Log.e(tag, Log.getStackTraceString(t));
		}
	}
	
	public static void verbose(String tag, String msg) {
		if (Log.isLoggable(tag, Log.VERBOSE) && Debug.isDebuggerConnected()) {
			Log.v(tag, msg);
		}
	}
	
	public static void warn(String tag, String msg) {
		if (Log.isLoggable(tag, Log.WARN) && Debug.isDebuggerConnected()) {
			Log.w(tag, msg);
		}
	}
	
	public static void warn(String tag, String msg, Throwable t) {
		if (Log.isLoggable(tag, Log.WARN) && Debug.isDebuggerConnected()) {
			Log.w(tag, msg, t);
			Log.w(tag, Log.getStackTraceString(t));
		}
	}
}
