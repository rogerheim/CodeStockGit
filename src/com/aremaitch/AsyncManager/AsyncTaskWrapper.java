package com.aremaitch.AsyncManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

public class AsyncTaskWrapper {
	private WeakReference<AsyncTask> _wrappedTask2 = null;
	
//	private AsyncTask _wrappedTask = null;
	private Activity _act = null;
	private ProgressDialog _progress = null;
	private UUID _taskKey = null;
	
	public AsyncTaskWrapper(UUID taskKey, AsyncTask task, Activity act) {
		this._taskKey = taskKey;
		this._act = act;
//		this._wrappedTask = task;
		this._wrappedTask2 = new WeakReference<AsyncTask>(task);
	}
	
	//	readonly
	public UUID getTaskKey() {
		return _taskKey;
	}
	
	public void setTaskActivity(Activity newActivity) {
		if (newActivity == null) {
			throw new NullPointerException("Cannot pass null as newActivity (use clearActivity() instead)");
		}
		
		if (this._act != null && this._act.equals(newActivity)) {
			//  Trying to set to ourselves; just return
			return;
		}
		this._act = newActivity;
	}
	
	public void clearTaskActivity() {
		clearProgressDialog();
		this._act = null;
	}
	
	/**
	 * Attempts to kill the wrapped task.
	 * @return True if the task was killed, false othwerwise (typically because it was already finished.) This is an
	 * immediate kill and the thread is unceremoniously nuked.
	 */
	public boolean killTask() {
//		return this._wrappedTask.cancel(true);
		AsyncTask at = this._wrappedTask2.get();
		if (at != null) {
			return at.cancel(true);
		}
		return false;
	}
	
	/**
	 * Displays a ProgressDialog associated with the wrapped AsyncTask.
	 * @param dialogTitle A String to use as the title of the ProgressDialog.
	 * @param dialogMsg A String to use as the message of the ProgressDialog.
	 */
	public void showProgressDialog(String dialogTitle, String dialogMsg) {
		if (this._act == null) {
			throw new IllegalStateException("Cannot show progress dialog with null activity");
		}
		_progress = ProgressDialog.show(this._act, 
				dialogTitle, dialogMsg);
	}
	
	/**
	 * Clears the ProgressDialog associated with the wrapped AsyncTask.
	 */
	public void clearProgressDialog() {
		if (_progress != null) {
			_progress.dismiss();
		}
	}
	
	/**
	 * Returns the wrapped AsyncTask.
	 * @return The AsyncTask wrapped by this AsyncTaskWrapper.
	 */
	public AsyncTask getTask() {
//		return this._wrappedTask;
		return this._wrappedTask2.get();
	}
	
	/**
	 * Returns true if the task is still running or is pending.
	 * @return Boolean true if the task is not finished, false otherwise.
	 */
	public boolean isTaskStillRunning() {
//		return !(this._wrappedTask.getStatus() == AsyncTask.Status.FINISHED);
		boolean result = false;
		AsyncTask at = this._wrappedTask2.get();
		if (at != null) {
			result = !(at.getStatus() == AsyncTask.Status.FINISHED);
		}
		return result;
	}
	
	/**
	 * Invokes a callback method on whatever Activity is currently associated with the
	 * wrapped AsyncTask. The callback method must be a public method in the Activity that accepts
	 * a single argument of type Bundle.
	 * @param methodName A String containing the name of the callback method to invoke.
	 * @param callbackArgs A Bundle containing the arguments to pass to the callback method. Use null if there are no arguments to pass.
	 */
	public void invokeOnActivity(String methodName, Bundle callbackArgs) {
		try {
			Method callback = this._act.getClass().getMethod(methodName, new Class[] {Bundle.class});
			Class[] testArgs = callback.getParameterTypes();
			
			callback.invoke(this._act, callbackArgs);
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void invokeOnActivity(String methodName, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
		try {
			Method callback = this._act.getClass().getMethod(methodName, new Class[] {Object.class, Object.class, Object.class, Object.class, Object.class});
			Class[] testArgs = callback.getParameterTypes();
			
			callback.invoke(this._act, arg0, arg1, arg2, arg3, arg4);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		
	}
}
