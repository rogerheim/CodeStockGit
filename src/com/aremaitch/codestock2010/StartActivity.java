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

//import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.aremaitch.codestock2010.datadownloader.Downloader;
import com.aremaitch.codestock2010.datadownloader.DownloaderV2;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

//	Theme.NoTitleBar hides the app's title bar.
//	Theme.NoTitleBar.Fullscreen also covers the notification bar.
//	See http://developer.android.com/intl/fr/reference/android/R.style.html

public class StartActivity extends Activity {
	//private static final String CODESTOCK_TAG = "CodeStock2010";
	private static final int MENU_REFRESH = Menu.FIRST;
	private static final int MENU_HOME = Menu.FIRST + 1;
	private static final int MENU_SEARCH = Menu.FIRST + 2;
	
	private static final String DOWNLOAD_TASK_KEY = "download_data";
	private static final String INITIALIZE_JODA_TASK_KEY = "init_joda";
	private static final String UPDATE_DB_TASK_KEY = "update_db";
	
//	private boolean jodaInit = false;
	
	//private static final DateTime dt = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//	Called once when the activity is started.
		super.onCreate(savedInstanceState);
		Log.v(getString(R.string.logging_tag), "StartActivity onCreate");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.startup_activity);
		
		initializeApp();
		
		wireupListeners();
		
		
		DataHelper localdh = new DataHelper(this);
		boolean databaseIsEmpty = localdh.isDatabaseEmpty();
		localdh.close();
		//	If the database is empty and we are not curently loading it, ask the user if they
		//	want to load it.
		if (databaseIsEmpty && !isDataLoading()) {
			new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(getString(R.string.empty_db_msg))
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
				})
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						((CodeStockApp)getApplication()).pushTask(DOWNLOAD_TASK_KEY, 
								new RefreshCodeStockData(StartActivity.this).execute());
//						RefreshCodeStockData task = new RefreshCodeStockData();
//						task.execute();
					}
				})
				.setTitle(getString(R.string.app_name))
				.show();
			
		}
	}
	
	private boolean isDataLoading() {
		boolean result = false;
		CodeStockApp theApp = (CodeStockApp)getApplication();
		
		if (theApp.getTask(DOWNLOAD_TASK_KEY) != null ||
			theApp.getTask(UPDATE_DB_TASK_KEY) != null) {
			result = true;
		}
		return result;
		
	}
	
	private void initializeApp() {
		if (((CodeStockApp)getApplication()).getTask(INITIALIZE_JODA_TASK_KEY) != null) {
			//	Already a task running; just return
			return;
		}
		
//		if (!jodaInit) {
//			((CodeStockApp)getApplication()).pushTask(INITIALIZE_JODA_TASK_KEY, 
//				new InitializeJodaAsync(this).execute());
//			jodaInit = true;
//		}
	}

	@Override
	protected void onStart() {
		//	Called after onCreate() or after the application was previously not visible
		//	(but still running) and is now visible.
		super.onStart();
		Log.v(getString(R.string.logging_tag), "StartActivity onStart");
	}
	
	@Override
	protected void onPause() {
		//	Called when another activity comes in front of this one. This activity may
		//	still be visible but is no longer on top.
		super.onPause();
		Log.v(getString(R.string.logging_tag), "StartActivity onPause");
	}

	@Override
	protected void onStop() {
		//	Called when we are no longer visible.
		super.onStop();
		Log.v(getString(R.string.logging_tag), "StartActivity onStop");

		clearActivityFromAsyncTasks();
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v(getString(R.string.logging_tag), "StartActivity onSaveInstanceState");
		clearActivityFromAsyncTasks();
		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		//	This is called after onStart()
		super.onRestoreInstanceState(savedInstanceState);
		Log.v(getString(R.string.logging_tag), "StartActivity onRestoreInstanceState");
		assignActivityToAsyncTasks();
	}
	
	private void assignActivityToAsyncTasks() {
		CodeStockApp theApp = (CodeStockApp)getApplication();
		
		RefreshCodeStockData task1 = (RefreshCodeStockData)theApp.getTask(DOWNLOAD_TASK_KEY);
		if (task1 != null) {
			task1.setActivity(this);
		}
		
//		InitializeJodaAsync task2 = (InitializeJodaAsync)theApp.getTask(INITIALIZE_JODA_TASK_KEY);
//		if (task2 != null) {
//			task2.setActivity(this);
//		}
		
		SessionDatabaseUpdater task3 = (SessionDatabaseUpdater)theApp.getTask(UPDATE_DB_TASK_KEY);
		if (task3 != null) {
			task3.setActivity(this);
		}
	}
	
	private void clearActivityFromAsyncTasks() {
		CodeStockApp theApp = (CodeStockApp)getApplication();
		
		RefreshCodeStockData task1 = (RefreshCodeStockData)theApp.getTask(DOWNLOAD_TASK_KEY);
		if (task1 != null) {
			Log.v(getString(R.string.logging_tag), "Clearing download task activity");
			task1.setActivity(null);
		}
		
//		InitializeJodaAsync task2 = (InitializeJodaAsync)theApp.getTask(INITIALIZE_JODA_TASK_KEY);
//		if (task2 != null) {
//			Log.v(getString(R.string.logging_tag), "Clearing init joda task activity");
//			task2.setActivity(null);
//		}
		
		SessionDatabaseUpdater task3 = (SessionDatabaseUpdater)theApp.getTask(UPDATE_DB_TASK_KEY);
		if (task3 != null) {
			Log.v(getString(R.string.logging_tag), "Clearing update db task activity");
			task3.setActivity(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, 0, getString(R.string.menu_refresh_text)).setIcon(R.drawable.ic_menu_refresh);
		menu.add(0, MENU_HOME, 0, getString(R.string.menu_home_text)).setIcon(R.drawable.ic_menu_home);
		menu.add(0, MENU_SEARCH, 0, getString(R.string.menu_search_text)).setIcon(android.R.drawable.ic_menu_search);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			((CodeStockApp)getApplication()).pushTask(DOWNLOAD_TASK_KEY, 
					new RefreshCodeStockData(StartActivity.this).execute());
//			RefreshCodeStockData task = new RefreshCodeStockData();
//			task.execute();
			
			return true;
		}
		
		return false;
	}
	
	
	private void wireupListeners() {
		ImageButton scheduleButton = (ImageButton) findViewById(R.id.schedule_imagebutton);
		scheduleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(getString(R.string.logging_tag), "Schedule button onClick");
			}
		});
		
		
		ImageButton mapButton = (ImageButton) findViewById(R.id.map_imagebutton);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(getString(R.string.logging_tag), "Map button onClick");
				Intent i = new Intent();
				i.setAction(getString(R.string.conference_center_map_intent_action))
					.addCategory(getString(R.string.conference_center_map_intent_category));
				startActivity(i);
				
			}
		});
		
		
		ImageButton sessionsButton = (ImageButton) findViewById(R.id.sessions_imagebutton);
		sessionsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(getString(R.string.logging_tag), "Sessions button onClick");
				Intent i = new Intent();
				i.setAction(getString(R.string.sessions_intent_action))
					.addCategory(getString(R.string.sessions_intent_category));
				startActivity(i);
				
			}
		});
		
		
		ImageButton starredButton = (ImageButton) findViewById(R.id.starred_imagebutton);
		starredButton.setOnClickListener(new OnClickListener() {
			
			//	This works however, after the scan an error occurs in MySessionsActivity.
			//	Running it again (after the userid has been pref'd) results in no error.
			@Override
			public void onClick(View v) {
				Log.i(getString(R.string.logging_tag), "Starred button onClick");
				SharedPreferences settings = getSharedPreferences("CodeStock2010Prefs", Context.MODE_PRIVATE);
				long userid = settings.getLong("userid", 0);
				if (userid == 0) {
					promptUserToScanQRCode();
				} else {
					startMySessions(userid);
				}
			
			}
		});
		
		
		ImageButton mynotesButton = (ImageButton) findViewById(R.id.mynotes_imagebutton);
		mynotesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(getString(R.string.logging_tag), "MyNotes button onClick");
				
			}
		});
		
		
		ImageButton creditsButton = (ImageButton) findViewById(R.id.credits_imagebutton);
		creditsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(getString(R.string.logging_tag), "Credits button onClick");
				Intent i = new Intent();
				i.setAction(getString(R.string.about_intent_action))
					.addCategory(getString(R.string.about_intent_category));
				startActivity(i);
			}
		});
	}
	
	private void promptUserToScanQRCode() {
		
		// This dialog doesn't show until onCreate() shows.

		new AlertDialog.Builder(this).setCancelable(false)
			.setMessage("To use My Sessions you need to first build your schedule using the CodeStock website. " +
				 "The web site will show you a QR barcode you can scan that will link it with your phone. "	 +
				 "Note that you need a free barcode scanning app on your phone, which you will be prompted to "	 +
				 "download if you don't have it. Do you want to continue?")
			.setNegativeButton("No", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,	int which) {
					dialog.dismiss();
					IntentIntegrator.initiateScan(StartActivity.this);
				}
			})
			.setTitle("Schedule Builder")
			.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			//	result should be the url: http://codestock.org/ViewSchedule.aspx?id=111
			//	find the last position of 'id=', bump by 3 to point to number, then parse it to a long.
			String result = scanResult.getContents();
			if (!TextUtils.isEmpty(result)) {
				Long userid = Long.parseLong(result.substring(result.lastIndexOf("id=") + 3));
				SharedPreferences.Editor editor = getSharedPreferences("CodeStock2010Prefs", Context.MODE_PRIVATE).edit();
				editor.putLong("userid", userid);
				editor.commit();
				startMySessions(userid);
			}
		}
	}

	private void startMySessions(long userid) {
		Intent i = new Intent();
		i.setAction(getString(R.string.mysessions_intent_action))
			.addCategory(Intent.CATEGORY_DEFAULT)
			.putExtra("userid", userid);
		startActivity(i);
		

	}
//	private class InitializeJodaAsync extends AsyncTask<Void, Void, Void >{
//		ProgressDialog progress = null;
//		Activity _act = null;
//		
//		public InitializeJodaAsync(Activity act) {
//			super();
//			_act = act;
//		}
//		
//		public void setActivity(Activity act ) {
//			_act = act;
//			if (_act == null) {
//				if (progress != null) {
//					progress.dismiss();
//				}
//			} else {
//				showProgressDialog();
//			}
//		}
//		
//		private void clearActivity() {
//			if (_act != null) {
//				((CodeStockApp)_act.getApplication()).clearTask(INITIALIZE_JODA_TASK_KEY);
//			}
//		}
//		
//		private void showProgressDialog() {
//			if (_act != null) {
////				progress = ProgressDialog.show(_act, "Initializing", "Initializing CodeStock 2010;\n\nPlease wait...");
//			}
//		}
//		@Override
//		protected void onPreExecute() {
//			if (_act != null) {
//				showProgressDialog();
//			}
//		}
//		
//		@Override
//		protected void onCancelled() {
//			if (progress != null) {
//				progress.dismiss();
//			}
//			clearActivity();
//		}
//		
//		@Override
//		protected Void doInBackground(Void... params) {
//			//	New up a DateTime object so Joda can create its static objects.
//			new DateTime();
//			return null;
//		}
//		
//		@Override
//		protected void onPostExecute(Void result) {
//			if (_act != null && progress != null) {
//				progress.dismiss();
//			}
//			clearActivity();
//		}
//	}

	
	private class RefreshCodeStockData extends AsyncTask<Void, Void, Void> {

		ProgressDialog progress = null;
//		Downloader dl = null;
		DownloaderV2 dlv2 = null;
		Activity _act = null;
		
		public RefreshCodeStockData(Activity act) {
			super();
			_act = act;
		}
		
		public void setActivity(Activity act) {
			_act = act;
			if (_act == null) {
				if (progress != null) {
					progress.dismiss();
				}
			} else {
				showProgressDialog();
			}
		}

		private void clearActivity() {
			((CodeStockApp)_act.getApplication()).clearTask(DOWNLOAD_TASK_KEY);
		}
		
		private void showProgressDialog() {
			if (_act != null) {
				progress = ProgressDialog.show(_act, "Refresh",  getString(R.string.refresh_data_progress_dialog_msg));
			}
		}
		@Override
		protected void onPreExecute() {
			if (_act != null) {
				showProgressDialog();
			}
		}
		
		@Override
		protected void onCancelled() {
			progress.dismiss();
			clearActivity();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
//			dl = new Downloader(_act, _act.getString(R.string.json_data_url));
			dlv2 = new DownloaderV2(_act, 
					_act.getString(R.string.json_data_rooms_url_v2), 
					_act.getString(R.string.json_data_speakers_url_v2), 
					_act.getString(R.string.json_data_sessions_url_v2));
			dlv2.getCodeStockData();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			if (_act != null) {
//				((CodeStockApp)getApplication()).pushTask(UPDATE_DB_TASK_KEY, 
//						new SessionDatabaseUpdater(_act, dl).execute());
				((CodeStockApp)getApplication()).pushTask(UPDATE_DB_TASK_KEY, 
						new SessionDatabaseUpdater(_act, dlv2).execute());
				progress.dismiss();
			}
			clearActivity();
		}
	}

	
	private class SessionDatabaseUpdater extends AsyncTask<Void, Void, Void> {
//		Downloader dl = null;
		DownloaderV2 dlv2 = null;
		
		ProgressDialog progress = null;
		Activity _act = null;
		
//		public SessionDatabaseUpdater(Activity act, Downloader dl) {
//			this.dl = dl;
//			_act = act;
//		}

		public SessionDatabaseUpdater(Activity act, DownloaderV2 dlv2) {
			this.dlv2 = dlv2;
			_act = act;
		}
		
		public void setActivity(Activity act) {
			_act = act;
			if (_act == null) {
				if (progress != null) {
					progress.dismiss();
				}
			} else {
				showProgressDialog();
			}
		}
		
		private void clearActivity(){
			((CodeStockApp)_act.getApplication()).clearTask(UPDATE_DB_TASK_KEY);
		}
		
		private void showProgressDialog() {
			progress = ProgressDialog.show(_act, "Refresh", _act.getString(R.string.update_db_progress_dialog_msg));
		}
		
		@Override
		protected void onPreExecute() {
			if (_act != null) {
				showProgressDialog();
			}
		}
		
		@Override
		protected void onCancelled() {
			progress.dismiss();
			clearActivity();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (_act != null) {
				progress.dismiss();
			}
			clearActivity();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			DataHelper dh = new DataHelper(_act);
			dh.clearAllData();
			
			try {
				for (Track t : dlv2.getParsedTracks()) {
					dh.insertTrack(t);
				}
				for (ExperienceLevel l : dlv2.getParsedLevels()) {
					dh.insertXPLevel(l);
				}
				for (Speaker s : dlv2.getParsedSpeakers()) {
					dh.insertSpeaker(s);
				}
				for (Session s : dlv2.getParsedSessions()) {
					dh.insertSession(s);
				}
			} finally {
				dh.close();
			}
			return null;
		}
	}
}
