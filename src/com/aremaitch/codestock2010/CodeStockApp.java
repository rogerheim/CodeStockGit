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

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.os.AsyncTask;

public class CodeStockApp extends Application {
	private Map<String, AsyncTask> _currentTasks = null;

	public CodeStockApp() {
		_currentTasks = new HashMap<String, AsyncTask>();
	}
	public void pushTask(String taskKey, AsyncTask task) {
		_currentTasks.put(taskKey, task);
	}
	
	public AsyncTask getTask(String taskKey) {
		return _currentTasks.get(taskKey);
	}
	
	public void clearTask(String taskKey) {
		_currentTasks.remove(taskKey);
	}
	
}
