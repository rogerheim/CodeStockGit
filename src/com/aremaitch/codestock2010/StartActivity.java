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

import java.util.UUID;

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
import android.widget.TextView;
import android.widget.Toast;

import com.aremaitch.codestock2010.datadownloader.Downloader;
import com.aremaitch.codestock2010.datadownloader.DownloaderV2;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;
import com.aremaitch.utils.ACLogger;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

//	Theme.NoTitleBar hides the app's title bar.
//	Theme.NoTitleBar.Fullscreen also covers the notification bar.
//	See http://developer.android.com/intl/fr/reference/android/R.style.html

public class StartActivity extends Activity {
	private static final int MENU_REFRESH = Menu.FIRST;

	UUID backgroundTaskID = null;
	final String TASK_KEY = "backgroundTaskID";
	
	@Override
	protected void onDestroy() {
		ACLogger.info(getString(R.string.logging_tag), "StartActivity onDestroy");
		super.onDestroy();
	}
	
	@Override
	protected void finalize() throws Throwable {
		ACLogger.info(getString(R.string.logging_tag), "StartActivity finalize");
		super.finalize();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//	Called once when the activity is started.
		super.onCreate(savedInstanceState);
		ACLogger.info(getString(R.string.logging_tag), "StartActivity onCreate");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.startup_activity);
		
		
		
		//	Header is a standard include; change the text.
		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.header_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText(getString(R.string.header_slogan));
		
		
		wireupListeners();
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(TASK_KEY)) {
				backgroundTaskID = UUID.fromString(savedInstanceState.getString(TASK_KEY));
			}
		}
		
		boolean databaseIsEmpty = false;
		boolean isDataLoading = false;
		if (backgroundTaskID != null) {
			//  We were restarted during a data load (probably because of an orientation change.)
			//	Reshow the progress dialog.
			if (CodeStockApp.getTaskManagerInstance().isTaskStillRunning(backgroundTaskID)) {
				isDataLoading = true;
				CodeStockApp.getTaskManagerInstance()
					.setTaskParentAndShowProgressDialog(
							backgroundTaskID, 
							this, 
							getString(R.string.refresh_data_progress_dialog_title), 
							getString(R.string.refresh_data_progress_dialog_msg));
			} else {
				//	Background task id was set but the task is no longer running (or was already removed
				//	from task table;) null out the backgroundtask id.
				backgroundTaskID = null;
			}
		} else {
			DataHelper localdh = new DataHelper(this);
			databaseIsEmpty = localdh.isDatabaseEmpty();
			localdh.close();
		}
		
		//	If the database is empty and we are not curently loading it, ask the user if they
		//	want to load it.
		if (databaseIsEmpty && !isDataLoading) {
			new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(getString(R.string.empty_db_msg))
				.setNegativeButton(getString(R.string.no_string), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
				})
				.setPositiveButton(getString(R.string.yes_string), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						startDataLoad();
					}
				})
				.setTitle(getString(R.string.app_name))
				.show();
		}
	}
	
	private void startDataLoad() {
		//	No callback here. Actually we could do one to determine if we completed or were cancelled.
		RefreshCodeStockData task = new RefreshCodeStockData("");
		backgroundTaskID = CodeStockApp.getTaskManagerInstance()
			.createTask(task, 
					this, 
					getString(R.string.refresh_data_progress_dialog_title), 
					getString(R.string.refresh_data_progress_dialog_msg));
		task.execute();
	}
	
	
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//	If the progress dialog is showing, dismiss it before Android
		//	saves our state.
		
		ACLogger.info(getString(R.string.logging_tag), "StartActivity onSaveInstanceState");

		if (backgroundTaskID != null) {
			
			//	If the AsyncTaskWrapper uses WeakReferences to the activity do we need to
			//	worry about clearing the task parent?
			//	Still need to nuke progress dialog (or not?)
//			CodeStockApp.getTaskManagerInstance().clearTaskParent(backgroundTaskID);
			CodeStockApp.getTaskManagerInstance().clearProgressDialog(backgroundTaskID);
			outState.putString(TASK_KEY, backgroundTaskID.toString());
		}
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, 0, getString(R.string.menu_refresh_text)).setIcon(R.drawable.ic_menu_refresh);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			startDataLoad();
			return true;
		}
		
		return false;
	}
	
	
	private void wireupListeners() {
		//	Agenda
		ImageButton scheduleButton = (ImageButton) findViewById(R.id.schedule_imagebutton);
		scheduleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ACLogger.info(getString(R.string.logging_tag), "Schedule button onClick");
				startActivity(
						new Intent()
							.setAction(getString(R.string.agenda_intent_action))
							.addCategory(Intent.CATEGORY_DEFAULT));
				
			}
		});
		
		
		ImageButton mapButton = (ImageButton) findViewById(R.id.map_imagebutton);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ACLogger.info(getString(R.string.logging_tag), "Map button onClick");
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
				ACLogger.info(getString(R.string.logging_tag), "Sessions button onClick");
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
				ACLogger.info(getString(R.string.logging_tag), "Starred button onClick");
				SharedPreferences settings = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
				long userid = settings.getLong(getString(R.string.shared_prefs_userid), 0);
				if (userid == 0) {
					promptUserToScanQRCode();
				} else {
					startMySessions(userid);
				}
			
			}
		});
		
		
		
		
		ImageButton creditsButton = (ImageButton) findViewById(R.id.credits_imagebutton);
		creditsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ACLogger.info(getString(R.string.logging_tag), "Credits button onClick");
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
			.setMessage(getString(R.string.mysessions_qrscan_prompt_msg))
			.setNegativeButton(getString(R.string.no_string), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setPositiveButton(getString(R.string.yes_string), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,	int which) {
					dialog.dismiss();
					IntentIntegrator.initiateScan(StartActivity.this);
				}
			})
			.setTitle(getString(R.string.mysessions_qrscan_prompt_title))
			.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			//	result should be the url: http://codestock.org/ViewSchedule.aspx?id=111
			//	find the last position of 'id=', bump by 3 to point to number, then parse it to a long.
			
			//	17-Jun-10: In the emulator the call to initiateScan() returns immediately. scanResult is 
			//				a valid object but getContents() returns null.
			
			String result = scanResult.getContents();
			if (TextUtils.isEmpty(result)) {
//				Toast.makeText(this, "Scan results contents were empty; do you not have a camera?", Toast.LENGTH_SHORT).show();
				return;
			}
			
			//	17-Jun-10:	Looks like the format of the link changed. I love moving targets.
			if (result.toLowerCase().startsWith("http://codestock.org/viewschedule.aspx?id=")) {
				parseWebsiteLink(result);
				return;
			}
			
			if (result.toLowerCase().startsWith("http://codestock.org/m/viewschedule.aspx?id=")) {
				parseWebsiteLink(result);
				return;
			}
			
			Toast.makeText(this, 
					getString(R.string.mysessions_qrscan_badscan_msg), 
					Toast.LENGTH_LONG).show();
			return;
			
		}
	}

	private void parseWebsiteLink(String link) {
		if (link.lastIndexOf("id=") != -1) {
			Long userid = Long.parseLong(link.substring(link.lastIndexOf("id=") + 3));
			SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE).edit();
			editor.putLong(getString(R.string.shared_prefs_userid), userid);
			editor.commit();
			startMySessions(userid);
		}
	}

	private void startMySessions(long userid) {
		Intent i = new Intent();
		i.setAction(getString(R.string.mysessions_intent_action))
			.addCategory(Intent.CATEGORY_DEFAULT)
			.putExtra(getString(R.string.shared_prefs_userid), userid);
		startActivity(i);
	}

	
	public class RefreshCodeStockData extends AsyncTask<Void, Void, Void> {

		DownloaderV2 dlv2 = null;
		private String callbackMethod = "";
		
		public RefreshCodeStockData(String callbackMethod) {
			this.callbackMethod = callbackMethod;
		}

		@Override
		protected void onCancelled() {
			//	With WeakReferences do we really need to clearTaskParent?
//			CodeStockApp.getTaskManagerInstance().clearTaskParent(this);
			CodeStockApp.getTaskManagerInstance().removeTask(this, false);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			ACLogger.info(getString(R.string.logging_tag), "Starting RefreshCodeStockData.doInBackground()");
			//			dl = new Downloader(_act, _act.getString(R.string.json_data_url));
			dlv2 = new DownloaderV2(getApplicationContext(), 
					getString(R.string.json_data_rooms_url_v2), 
					getString(R.string.json_data_speakers_url_v2), 
					getString(R.string.json_data_sessions_url_v2));
			dlv2.getCodeStockData();
			
			DataHelper dh = new DataHelper(getApplicationContext());
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


		@Override
		protected void onPostExecute(Void result) {
			ACLogger.info(getString(R.string.logging_tag), "RefreshCodeStockData.onPostExecute()");
			
			if (!TextUtils.isEmpty(this.callbackMethod)) {
				Bundle args = new Bundle();
				args.putBoolean("Cancelled", false);
				args.putBoolean("Completed", true);
				CodeStockApp.getTaskManagerInstance().invokeOnActivity(this, this.callbackMethod, args);
			}
//			CodeStockApp.getTaskManagerInstance().clearTaskParent(this);
			CodeStockApp.getTaskManagerInstance().removeTask(this, false);
			super.onPostExecute(result);
		}
	}
}
