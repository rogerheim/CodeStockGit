/*
   Copyright 2010 Roger Heim

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package com.aremaitch.codestock2010;

import android.app.Application;

import com.aremaitch.AsyncManager.AsyncTaskManager;

public class CodeStockApp extends Application {
	
	private static CodeStockApp theApp;
	private static AsyncTaskManager taskMgr;
	
	
	public CodeStockApp() {
		super();
		theApp = this;
		taskMgr = AsyncTaskManager.getInstance();
	}
	
	public static CodeStockApp getInstance() {
		return theApp;
	}
	
	public static AsyncTaskManager getTaskManagerInstance() {
		if (taskMgr == null) {
			taskMgr = AsyncTaskManager.getInstance();
		}
		return taskMgr;
	}
}
