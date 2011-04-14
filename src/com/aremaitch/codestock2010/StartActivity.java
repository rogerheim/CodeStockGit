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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.aremaitch.codestock2010.library.*;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.ExperienceLevel;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.codestock2010.repository.Speaker;
import com.aremaitch.codestock2010.repository.Track;
import com.aremaitch.utils.ACLogger;
import com.aremaitch.utils.Command;
import com.aremaitch.utils.OnClickCommandWrapper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

//	Theme.NoTitleBar hides the app's title bar.
//	Theme.NoTitleBar.Fullscreen also covers the notification bar.
//	See http://developer.android.com/intl/fr/reference/android/R.style.html

//	Beginning revisions for 2011

public class StartActivity extends Activity {

//	RefreshCodeStockData task = null;
	ProgressDialog _progress = null;
	CountdownManager cMgr = new CountdownManager();
    StartActivityMenuManager menuManager = new StartActivityMenuManager(this);
    TweetDisplayManager tdm = null;
	View digitsContainer = null;

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);

    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//	Called once when the activity is started.
		super.onCreate(savedInstanceState);
		ACLogger.info(CSConstants.LOG_TAG, "StartActivity onCreate");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.startup_activity);
		setContentView(R.layout.countdown_startup_activity);
		
		digitsContainer = findViewById(R.id.countdown_digit_container);

		//	Header is a standard include; change the text.
		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.header_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText(getString(R.string.header_slogan));
		
		//	disable listeners for countdown
		//wireupListeners();

//		task = (RefreshCodeStockData)getLastNonConfigurationInstance();
		
		boolean databaseIsEmpty = false;
		
		
//		if (task != null) {
//			ACLogger.info(CSConstants.LOG_TAG, "StartActivity reconnecting to running RefreshCodeStockData task");
//			//  We were restarted during a data load (probably because of an orientation change.)
//			//	Reshow the progress dialog.
//			task.attach(this);
//			showProgressDialog();
//			if (task.getStatus() == AsyncTask.Status.FINISHED) {
//				clearProgressDialog();
//			}
//
//		} else {
//			DataHelper localdh = new DataHelper(this);
//			databaseIsEmpty = localdh.isDatabaseEmpty();
//			localdh.close();
//		}
		

//	removed for countdown update

		//	If the database is empty and we are not currently loading it, ask the user if they
		//	want to load it.
//        if (databaseIsEmpty && task == null) {
//            Command yesCommand = new Command() {
//                @Override
//                public void execute() {
//                    startDataLoad();
//                }
//            };
//            new AlertDialog.Builder(this)
//                .setCancelable(false)
//                .setMessage(getString(R.string.empty_db_msg))
//                .setNegativeButton(getString(R.string.no_string), new OnClickCommandWrapper(Command.NOOP))
//                .setPositiveButton(getString(R.string.yes_string), new OnClickCommandWrapper(yesCommand))
//                .setTitle(getString(R.string.app_name))
//                .show();
//        }
	}
	
	@Override
	protected void onPause() {
		cMgr.stop();
        stopTweetDisplay();
		super.onPause();
	}

    @Override
    protected void onResume() {
        BackgroundTaskManager btm = new BackgroundTaskManager(this);
        btm.cancelAllRecurringTasks();

        if (new CSPreferenceManager(this).isTwitterUpdateEnabled()) {
            btm.setDBCleanupTask();
            btm.setRecurringTweetScan();
        }

        cMgr.initializeCountdown(digitsContainer, getAssets());
        cMgr.start();

        startTweetDisplay();
        super.onResume();
    }

    @Override
	public Object onRetainNonConfigurationInstance() {
//		if (task != null) {
//			ACLogger.info(CSConstants.LOG_TAG, "StartActivity preparing for restart due to config change");
//			task.detach();
//			clearProgressDialog();
//			return task;
//		}
		return null;
	}

    private void startTweetDisplay() {
        CSPreferenceManager preferenceManager = new CSPreferenceManager(this);

        if (preferenceManager.isTwitterUpdateEnabled()) {
            tdm = new TweetDisplayManager(this,
                    AnimationUtils.loadAnimation(this, R.anim.tweet_fade_in),
                    AnimationUtils.loadAnimation(this, R.anim.tweet_fade_out),
                    findViewById(R.id.tweet_view_0),
                    findViewById(R.id.tweet_view_1),
                    preferenceManager.getTweetDisplayDuration());
            tdm.startTweetDisplayTimer();
        }
    }

    private void stopTweetDisplay() {
        if (tdm != null) {
            tdm.stopTweetDisplayTimer();
        }
    }

	void showProgressDialog() {
		_progress = ProgressDialog.show(this, getString(R.string.refresh_data_progress_dialog_title),
                getString(R.string.refresh_data_progress_dialog_msg));
	}
	
	void clearProgressDialog() {
		if (_progress != null) {
			_progress.dismiss();
			_progress = null;
		}
	}
	
//	private void startDataLoad() {
//		//	No callback here. Actually we could do one to determine if we completed or were cancelled.
//		task = new RefreshCodeStockData(this);
//		task.execute();
//		showProgressDialog();
//	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return menuManager.createStartActivityOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        return menuManager.startActivityOptionsItemSelected(item);
	}
	
	
	private void wireupListeners() {
		//	Agenda
		ImageButton scheduleButton = (ImageButton) findViewById(R.id.schedule_imagebutton);
		scheduleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ACLogger.info(CSConstants.LOG_TAG, "Schedule button onClick");
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
				ACLogger.info(CSConstants.LOG_TAG, "Map button onClick");
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
				ACLogger.info(CSConstants.LOG_TAG, "Sessions button onClick");
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
				ACLogger.info(CSConstants.LOG_TAG, "Starred button onClick");
				long userid = new CSPreferenceManager(StartActivity.this).getScheduleUserId();
				if (userid == 0) {
					promptUserToScanQRCode();
				} else {
					startMySessions(userid);
				}
			
			}
		});
		
	}
	
	private void promptUserToScanQRCode() {

        Command yesCommand = new Command() {
            @Override
            public void execute() {
                IntentIntegrator.initiateScan(StartActivity.this);
            }
        };

		// This dialog doesn't show until onCreate() shows.

		new AlertDialog.Builder(this).setCancelable(false)
			.setMessage(getString(R.string.mysessions_qrscan_prompt_msg))
			.setNegativeButton(getString(R.string.no_string), new OnClickCommandWrapper(Command.NOOP))
			.setPositiveButton(getString(R.string.yes_string), new OnClickCommandWrapper(yesCommand))
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

            new CSPreferenceManager(this).setScheduleUserId(userid);
			startMySessions(userid);
		}
	}

	private void startMySessions(long userid) {

		Intent i = new Intent();
		i.setAction(getString(R.string.mysessions_intent_action))
			.addCategory(Intent.CATEGORY_DEFAULT)
			.putExtra(CSConstants.SCHEDULE_BUILDER_USERID_PREF, userid);
		startActivity(i);
	}

}
