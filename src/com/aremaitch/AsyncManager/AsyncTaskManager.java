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

package com.aremaitch.AsyncManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

//
//	Method signatures should specify the task identification first, then other
//	arguments afterward.
//
public class AsyncTaskManager {
	private HashMap<UUID, AsyncTaskWrapper> _taskList = null;
	private static AsyncTaskManager _mgr = null;
	
	private AsyncTaskManager() {
		_taskList = new HashMap<UUID, AsyncTaskWrapper>();
	}
	
	public static AsyncTaskManager getInstance() {
		if (_mgr == null) {
			_mgr = new AsyncTaskManager(); 
		}
		return _mgr;
	}

	public int getTaskCount() {
		return _taskList.size();
	}
	
	/**
	 * Takes an AsyncTaskWrapper, adds it to the list of running AsyncTasks and returns its ID.
	 * @param taskWrapper
	 * @return The UUID of the AsyncTaskWrapper.
	 */
	public UUID createTask(AsyncTaskWrapper taskWrapper) {
		UUID newID = UUID.randomUUID();
		synchronized (_taskList) {
			_taskList.put(newID, taskWrapper);
		}
		return newID;
	}
	
	
	/**
	 * Takes an AsyncTask and an Activity, wraps them in an AsyncTaskWrapper and returns the ID.
	 * @param task The AsyncTask.
	 * @param act The Activity that owns the AsyncTask
	 * @return The UUID of the AsyncTaskWrapper.
	 */
	public UUID createTask(AsyncTask task, Activity act) {
		UUID newID = UUID.randomUUID();
		AsyncTaskWrapper atw = new AsyncTaskWrapper(newID, task, act);
		synchronized (_taskList) {
			_taskList.put(newID, atw);
		}
		return newID;
	}
	
	
	public UUID createTask(AsyncTask task, Activity act, String dialogTitle, String dialogMsg) {
		UUID newID = UUID.randomUUID();
		AsyncTaskWrapper atw = new AsyncTaskWrapper(newID, task, act);
		synchronized (_taskList) {
			_taskList.put(newID, atw);
		}
		atw.showProgressDialog(dialogTitle, dialogMsg);
		return newID;
	}
	
	public void invokeOnActivity(UUID taskID, String methodName, Bundle callbackArgs) {
		AsyncTaskWrapper atw = getTaskWrapper(taskID);
		if (atw != null) {
			atw.invokeOnActivity(methodName, callbackArgs);
		}
	}
	
	public void invokeOnActivity(UUID taskID, String methodName, Object callbackArg0, Object callbackArg1, Object callbackArg2, Object callbackArg3, Object callbackArg4) {
		AsyncTaskWrapper atw = getTaskWrapper(taskID);
		if (atw != null) {
			atw.invokeOnActivity(methodName, callbackArg0, callbackArg1, callbackArg2, callbackArg3, callbackArg4);
		}
	}
	
	public void invokeOnActivity(AsyncTask task, String methodName, Bundle callbackArgs) {
		AsyncTaskWrapper atw = getTaskWrapper(task);
		if (atw != null) {
			atw.invokeOnActivity(methodName, callbackArgs);
		}
	}
	
	public void invokeOnActivity(AsyncTask task, String methodName, Object callbackArg0, Object callbackArg1, Object callbackArg2, Object callbackArg3, Object callbackArg4) {
		AsyncTaskWrapper atw = getTaskWrapper(task);
		if (atw != null) {
			atw.invokeOnActivity(methodName, callbackArg0, callbackArg1, callbackArg2, callbackArg3, callbackArg4);
		}
	}
	
//	public void returnAsyncResultAndCleanup(AsyncTask task, String methodName, Bundle callbackArgs) {
//		AsyncTaskWrapper atw = getTaskWrapper(task);
//		if (atw != null) {
//			atw.invo
//		}
//	}
	
	public boolean removeTask(UUID taskID, boolean killTaskIfRunning) {
		//	Check to see if the task is still running?
		//		If so, kill it first or don't remove it and throw exception?
		//		If it's running and we don't kill it removing the wrapper removes any chance
		//			to get at it. (leak).
		//	Dismiss any displayed ProgressDialog first?
		//	
		
		boolean continueWithRemoval = true;
		if (killTaskIfRunning && isTaskStillRunning(taskID)) {
			continueWithRemoval = killTask(taskID);
		}
		if (continueWithRemoval) {
			clearProgressDialog(taskID);
			synchronized (_taskList) {
				_taskList.remove(taskID);
			}
		}
		
		return continueWithRemoval;
	}
	
	public boolean removeTask(AsyncTask task, boolean killTaskIfRunning) {
		boolean continueWithRemoval = true;
		AsyncTaskWrapper atw = getTaskWrapper(task);
		if (killTaskIfRunning && atw.isTaskStillRunning()) {
			continueWithRemoval = atw.killTask();
		}
		if (continueWithRemoval) {
			atw.clearProgressDialog();
			synchronized (_taskList) {
				_taskList.remove(atw.getTaskKey());
			}
		}
		return continueWithRemoval;
	}
	
	public boolean killTask(UUID taskID) {
		return getTaskWrapper(taskID).killTask();
	}
	
	
	/**
	 * Assigns a parent Activity to an asynchronous task.
	 * @param taskID The UUID of the AsyncTaskWrapper containing the AsyncTask in question.
	 * @param parent The Activity to assign as the parent.
	 */
	public void setTaskParent(UUID taskID, Activity parent) {
		AsyncTaskWrapper atw = getTaskWrapper(taskID);// _taskList.get(taskID);
		if (atw != null) {
			atw.setTaskActivity(parent);
		}
	}
	
	/**
	 * Assigns a parent Activity to an asynchronous task.
	 * @param task The AsyncTask to which a parent Activity will be assigned.
	 * @param parent The Activity to assign as the parent.
	 */
	public void setTaskParent(AsyncTask task, Activity parent) {
		AsyncTaskWrapper atw = this.getTaskWrapper(task);
		if (atw != null) {
			atw.setTaskActivity(parent);
		}
	}
	
	/**
	 * Assigns a parent Activity to an asynchronous task and shows a ProgressDialog
	 * @param taskID The UUID of the AsyncTaskWrapper containing the AsyncTask in question.
	 * @param parent The Activity to assign as the parent.
	 * @param dialogTitle A String to use as the title of the ProgressDialog.
	 * @param dialogMsg A String to use as the message in the ProgressDialog.
	 */
	public void setTaskParentAndShowProgressDialog(UUID taskID, Activity parent, String dialogTitle, String dialogMsg) {
		AsyncTaskWrapper atw = getTaskWrapper(taskID);
		if (atw != null) {
			atw.setTaskActivity(parent);
			atw.showProgressDialog(dialogTitle, dialogMsg);
		}
	}
	
	/**
	 * Returns true if an asynchronous task is still running, false otherwise.
	 * @param taskID The UUID of the AsyncTaskWrapper containing the AsyncTask in question.
	 * @return Boolean true if the wrapped AsyncTask is still running, false otherwise.
	 */
	public boolean isTaskStillRunning(UUID taskID) {
		AsyncTaskWrapper atw = getTaskWrapper(taskID);
		if (atw != null) {
			//
			//	Could this be a race condition? How would I protect against that?
			return atw.isTaskStillRunning();
		}
		return false;
	}
	
	/**
	 * Allows a task to get its AsyncTaskWrapper.
	 * @param task The AsyncTask
	 * @return The AsyncTask's AsyncTaskWrapper or null if the task was not found.
	 */
	public AsyncTaskWrapper getTaskWrapper(AsyncTask task) {
		synchronized (_taskList) {
			for (AsyncTaskWrapper atw : _taskList.values()) {
				if (atw.getTask().equals(task)) {
					return atw;
				}
			}
		}
		return null;
	}
	
	/**
	 * Allows a task to get its AsyncTaskWrapper.
	 * @param id The UUID of the task.
	 * @return The AsyncTask's AsyncTaskWrapper or null if the task was not found.
	 */
	public AsyncTaskWrapper getTaskWrapper(UUID taskID) {
		synchronized (_taskList) {
			if (_taskList.containsKey(taskID)) {
				return _taskList.get(taskID);
			}
		}
		return null;
	}
	
	/**
	 * Displays a ProgressDialog associated with an asynchronous task.
	 * @param taskID The UUID of the asynchronous task.
	 * @param dialogTitle A String to use as the title of the ProgressDialog.
	 * @param dialogMsg A String to use as the message of the ProgressDialog.
	 */
	public void showProgressDialog(UUID taskID, String dialogTitle, String dialogMsg) {
		AsyncTaskWrapper atw = this.getTaskWrapper(taskID);
		if (atw != null) {
			atw.showProgressDialog(dialogTitle, dialogMsg);
		}
	}
	
	/**
	 * Displays a ProgressDialog associated with an asynchronous task.
	 * @param task The AsyncTask.
	 * @param dialogTitle A String to use as the title of the ProgressDialog.
	 * @param dialogMsg A String to use as the message of the ProgressDialog.
	 */
	public void showProgressDialog(AsyncTask task, String dialogTitle, String dialogMsg) {
		AsyncTaskWrapper atw = this.getTaskWrapper(task);
		if (atw != null) {
			atw.showProgressDialog(dialogTitle, dialogMsg);
		}
	}

	/**
	 * Dismisses a ProgressDialog.
	 * @param taskID The UUID of the asynchronous task whose ProgressDialog is to be dismissed.
	 */
	public void clearProgressDialog(UUID taskID) {
		AsyncTaskWrapper atw = this.getTaskWrapper(taskID);
		if (atw != null) {
			atw.clearProgressDialog();
		}
	}
	
	/**
	 * Dismisses a ProgressDialog.
	 * @param task The AsyncTask whose ProgressDialog is to be dismissed.
	 */
	public void clearProgressDialog(AsyncTask task) {
		AsyncTaskWrapper atw = this.getTaskWrapper(task);
		if (atw != null) {
			atw.clearProgressDialog();
		}
	}
	
	/**
	 * Removes the association between an Activity and an AsyncTask.
	 * @param taskKey The UUID of the asynchronous task whose association with an Activity should be clared.
	 */
	public void clearTaskParent(UUID taskKey) {
		AsyncTaskWrapper atw = getTaskWrapper(taskKey);		// _taskList.get(taskKey);
		if (atw != null) {
			atw.clearTaskActivity();
		}
	}
	
	/**
	 * Removes the association between an Activity and an AsyncTask.
	 * @param task The AsyncTask whose asssociation with an Activity should be cleared.
	 */
	public void clearTaskParent(AsyncTask task) {
		AsyncTaskWrapper atw = this.getTaskWrapper(task);
		if (atw != null) {
			atw.clearTaskActivity();
		}
	}
}
