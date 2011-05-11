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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import com.aremaitch.codestock2010.datadownloader.ScheduleBuilder;
import com.aremaitch.codestock2010.library.*;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.MiniSession;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.utils.ACLogger;
import com.flurry.android.FlurryAgent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/*
 * The idea is that the contents of the view is a ViewFlipper. The ViewFlipper
 * contains 2 layouts; each with a TextView and a ListView. The 1st layout is the user's
 * schedule for Friday and the 2nd layout is the user's schdule for Saturday. The user
 * should be able to flip back and forth between the two day by flinging (it is a touchscreen
 * after all.)
 */
public class MySessionsActivity extends Activity 
		implements OnTouchListener, AdapterView.OnItemClickListener {
	ViewFlipper flipper;
	ArrayList<MiniSession> day1Sessions = null;
	ArrayList<MiniSession> day2Sessions = null;
	long userid = 0;
	private SimpleDateFormat dateFormatter;
	float downXValue;
	int currentView = 0;
	
	ListView day1LV = null;
	ListView day2LV = null;
	ProgressDialog dlg = null;

    CountdownManager cMgr;
    View digitsContainer = null;
    QuickActionMenuManager qaMgr = null;

    public static void startMe(Context ctx) {
        Intent i = new Intent(ctx, MySessionsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_sessions);

        initializeCountdownClock();
        checkAndStartAgendaDownload();

		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.mysessions_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText("");
		
		ACLogger.debug("MySessionsActivity", "onStart");
		day1Sessions = new ArrayList<MiniSession>();
		day2Sessions = new ArrayList<MiniSession>();
		

		dateFormatter = new SimpleDateFormat(getString(R.string.standard_where_when_format_string));
		flipper = (ViewFlipper) findViewById(R.id.my_sessions_flipper);
		
		View day1View = findViewById(R.id.my_sessions_day_1);
		View day2View = findViewById(R.id.my_sessions_day_2);
		
		day1LV = (ListView)day1View.findViewById(android.R.id.list);
		day2LV = (ListView)day2View.findViewById(android.R.id.list);
		

		TextView tv1 = (TextView) day1View.findViewById(R.id.my_sessions_header_date);
		TextView tv2 = (TextView) day2View.findViewById(R.id.my_sessions_header_date);
		tv1.setText(getString(R.string.friday_string));
		tv2.setText(getString(R.string.saturday_string));
		
		ImageButton nextButton0 = (ImageButton)day1View.findViewById(R.id.my_sessions_forward_button);
		nextButton0.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flipViewToSaturday();
			}
		});
		ImageButton prevButton0 = (ImageButton)day1View.findViewById(R.id.my_sessions_back_button);
		prevButton0.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Do nothing; don't flip backwards from Friday.
			}
		});
		ImageButton nextButton1 = (ImageButton)day2View.findViewById(R.id.my_sessions_forward_button);
		nextButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Do nothing; don't flip forwards from Saturday.
			}
		});
		ImageButton prevButton1 = (ImageButton)day2View.findViewById(R.id.my_sessions_back_button);
		prevButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flipViewBackToFriday();
			}
		});
		
		//	Must add headerview before calling setAdapter()
		day1LV.setOnItemClickListener(this);
		day2LV.setOnItemClickListener(this);
		
		//	Can we still do this before the async task has completed?
		//	Or do we have to move it to the onComplete call from async?
		day1LV.setAdapter(new DayAdapter(this, day1Sessions));
		day2LV.setAdapter(new DayAdapter(this, day2Sessions));
		
		
		LinearLayout my_sessions_main = (LinearLayout) findViewById(R.id.my_sessions_main);
		my_sessions_main.setOnTouchListener((OnTouchListener) this);

        //  Get the stored userid from preferences. If not stored, prompt the user to
        //  scan the qr code.
        userid = new CSPreferenceManager(this).getScheduleUserId();
        if (userid == 0) {
            //  Don't have their userid yet.
            promptUserToScanQRCode();
        } else {
            dlg = new ProgressDialog(this);
            new GetScheduleBuilderSessions(this, userid).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Change User");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //  The user wants to respecify the userid.
        promptUserToScanQRCode();
        return true;
    }

    @Override
    protected void onStart() {
        AnalyticsManager.logStartSession(this);
        super.onStart();
    }

    @Override
	protected void onStop() {
		super.onStop();
		if (dlg != null) {
			dlg.dismiss();
		}
        AnalyticsManager.logEndSession(this);
	}

    private void initializeCountdownClock() {
        cMgr = new CountdownManager();
        digitsContainer = findViewById(R.id.countdown_digit_container);
    }

    @Override
    protected void onResume() {
        qaMgr = new QuickActionMenuManager(findViewById(R.id.footer_logo));
        qaMgr.initializeQuickActionMenu();
        startCountdownClock();
        super.onResume();
    }

    private void startCountdownClock() {
        cMgr.initializeCountdown(digitsContainer, getAssets());
        cMgr.start();
    }

    @Override
    protected void onPause() {
        qaMgr.destroyQuickActionMenu();
        stopCountdownClock();
        super.onPause();
    }

    private void stopCountdownClock() {
        cMgr.stop();
    }

    @Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				downXValue = event.getX();
				break;
			}
			case MotionEvent.ACTION_UP: {
				float currentX = event.getX();
				if (downXValue < currentX && currentView == 1) {
					flipViewBackToFriday();
				} else if (downXValue > currentX && currentView == 0) {
					flipViewToSaturday();
				}
				break;
			}
		}
		return true;
	}
	
	private void flipViewBackToFriday() {
		flipper.setInAnimation(this, R.anim.slide_right_in);
		flipper.setOutAnimation(this, R.anim.slide_right_out);
		flipper.showPrevious();
		currentView = 0;
	}
	
	private void flipViewToSaturday() {
		flipper.setInAnimation(this, R.anim.slide_left_in);
		flipper.setOutAnimation(this, R.anim.slide_left_out);
		flipper.showNext();
		currentView = 1;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		//	If they click the day header, flip to the other day.
		if (v instanceof LinearLayout) {
			String tag = v.getTag().toString();
			if (tag.equalsIgnoreCase("day:friday")) {
				flipViewToSaturday();
			} else if (tag.equalsIgnoreCase("day:saturday")) {
				flipViewBackToFriday();
			}
			return;
		}
		
		//	Otherwise, show the details about the selected session.
        DisplaySessionDetailsActivity.startMe(this, id);
	}

    private static final int REQUEST_USERID_PROMPT = 19630809;
    private static final int REQUEST_REGEMAIL_PROMPT = 19630810;
    // The scanner's request code is 195543262

    private void promptUserToScanQRCode() {
        startActivityForResult(new Intent(this, GetCSUserIDActivity.class), REQUEST_USERID_PROMPT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_USERID_PROMPT:
                handleQRPromptResult(resultCode, intent);
                break;

            case REQUEST_REGEMAIL_PROMPT:
                handleEMailPromptResult(resultCode, intent);
                break;

            default:
                handleScanResult(requestCode, resultCode, intent);
                break;
        }
    }

    private void handleEMailPromptResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            userid = new ScheduleBuilder(userid).getUserIDFromEmail(intent.getStringExtra("email"));
            if (userid != 0) {
                new CSPreferenceManager(this).setScheduleUserId(userid);
                dlg = new ProgressDialog(this);
                new GetScheduleBuilderSessions(this, userid).execute();
                return;
            }
        }
    }

    private void handleQRPromptResult(int resultCode, Intent intent) {
        switch (resultCode) {
            case GetCSUserIDActivity.RESULT_QRCODE:
                IntentIntegrator.initiateScan(MySessionsActivity.this);
                break;

            case GetCSUserIDActivity.RESULT_EMAIL:
                startActivityForResult(new Intent(this, GetCSUserEmailActivity.class), REQUEST_REGEMAIL_PROMPT);
                break;

            default:
                finish();       // bail out from here
                break;
        }
    }

    private void handleScanResult(int requestCode, int resultCode, Intent intent) {
        
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            //	result should be the url: http://codestock.org/ViewSchedule.aspx?id=111
            //	find the last position of 'id=', bump by 3 to point to number, then parse it to a long.

            //	17-Jun-10: In the emulator the call to initiateScan() returns immediately. scanResult is
            //				a valid object but getContents() returns null.

            String result = scanResult.getContents();
            if (TextUtils.isEmpty(result)) {
                return;
            }

            //	17-Jun-10:	Looks like the format of the link changed. I love moving targets.
            if (result.toLowerCase().startsWith("http://codestock.org/viewschedule.aspx?id=")) {
                parseWebsiteLink(result);
                return;
            }

            if (result.toLowerCase().startsWith("http://codestock.org/m/viewschedule.aspx?id=")) {
                parseWebsiteLink(result);
                dlg = new ProgressDialog(this);

                //	There was a complaint in the Market about not using AsyncTask; this is probably what the user was
                //	complaining about.

                new GetScheduleBuilderSessions(this, userid).execute();
                return;
            }

            Toast.makeText(this,
                    getString(R.string.mysessions_qrscan_badscan_msg),
                    Toast.LENGTH_LONG).show();


        } else {
            finish();
        }
    }

    private void parseWebsiteLink(String link) {
        if (link.lastIndexOf("id=") != -1) {
            userid = Long.parseLong(link.substring(link.lastIndexOf("id=") + 3));

            new CSPreferenceManager(this).setScheduleUserId(userid);

        }
    }

    private void checkAndStartAgendaDownload() {
        DataHelper dh = null;
        try {
            dh = new DataHelper(this);
            if (dh.isDatabaseEmpty()) {
                CSAgendaDownloadSvc.startMe(this);
            }
        } finally {
            if (dh != null) {
                dh.close();
            }
        }
    }


	//	This needs the same protection against leaking windows
	private class GetScheduleBuilderSessions extends AsyncTask<Void, Void, Void> {

		long userid = 0;
//		Activity act = null;
		
		public GetScheduleBuilderSessions(Activity act, long userid) {
			//	Java rules state that the compiler will insert a call to the 
			//	parameterless constructor if there isn't already one.
			this.userid = userid;
//			this.act = act;
			
				
		
		}
		
		@Override
		protected void onPreExecute() {

			//	This moves ownership of the progress dialog to the activity instead of this class.
			dlg.setTitle(getString(R.string.mysessions_progress_title));
			dlg.setMessage(getString(R.string.mysessions_progress_message));
			dlg.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Long> userSessions = getUserSessions();
			splitUserSessions(userSessions);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dlg != null)
				dlg.dismiss();
			((BaseAdapter)day1LV.getAdapter()).notifyDataSetChanged();
			((BaseAdapter)day2LV.getAdapter()).notifyDataSetChanged();
		}
		
		private ArrayList<Long> getUserSessions() {
			ScheduleBuilder sb = new ScheduleBuilder( 
					this.userid);
			
			//	This list is already sorted chronologically.
			ArrayList<Long> userSessions = sb.getBuiltSchedule();
			return userSessions;
//			splitUserSessions(userSessions);
		}
		
		/**
		 * Splits array of long session id's into MiniSessions by day.
		 * @param userSessions
		 */
		private void splitUserSessions(ArrayList<Long> userSessions) {
//			DataHelper dh = new DataHelper(this.act);
			DataHelper dh = new DataHelper(getApplicationContext());
			
			try {
				for (Long sessionid : userSessions) {
					Session s = dh.getSession(sessionid);
					if (s != null) {
						MiniSession ms = new MiniSession();
						ms.setId(s.getId());
						ms.setRoom(s.getRoom());
						ms.setSessionTitle(s.getSessionTitle());
						ms.setSpeakerName(s.getSpeaker().getSpeakerName());
						ms.setStartDateTime(s.getStartDate());
						ms.setVoteRank(s.getVoteRank());
						
						if (s.getStartDate().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
							day1Sessions.add(ms);
						} else if (s.getStartDate().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
							day2Sessions.add(ms);
						}
					}
				}
			} finally {
				dh.close();
			}
		}
	
		
	}
	
	/**
	 * Efficient adapter for the two listviews.
	 * @author roger
	 *
	 */
	class DayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ArrayList<MiniSession> daySessions;
		
		public DayAdapter(Context context, ArrayList<MiniSession> daySessions) {
			mInflater = LayoutInflater.from(context);
			this.daySessions = daySessions;
		}
		
		@Override
		public int getCount() {
			return daySessions.size();
		}

		@Override
		public Object getItem(int position) {
			return daySessions.get(position);
		}

		@Override
		public long getItemId(int position) {
			return daySessions.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DayViewHolder holder;
			MiniSession ms = daySessions.get(position);
			
			if (convertView == null ) {
				convertView = mInflater.inflate(R.layout.my_sessions_list_item, null);
				holder = new DayViewHolder();
				holder.awardIV = (ImageView) convertView.findViewById(R.id.my_sessions_award);
				holder.dateTimeTV = (TextView) convertView.findViewById(R.id.my_sessions_datetime);
				holder.roomTV = (TextView) convertView.findViewById(R.id.my_sessions_room);
				holder.sessionTitleTV = (TextView) convertView.findViewById(R.id.my_sessions_session_title);
				holder.speakerNameTV = (TextView) convertView.findViewById(R.id.my_sessions_speaker_name);
				convertView.setTag(holder);
			} else {
				holder = (DayViewHolder)convertView.getTag();
			}
			holder.sessionid = ms.getId();
			holder.sessionTitleTV.setText(ms.getSessionTitle());
			
			if (TextUtils.isEmpty(ms.getVoteRank()) || ms.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_none))) {
				holder.awardIV.setVisibility(View.INVISIBLE);
			} else if (ms.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top1))) {
				holder.awardIV.setImageResource(R.drawable.top1);
				holder.awardIV.setVisibility(View.VISIBLE);
			} else if (ms.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top5))) {
				holder.awardIV.setImageResource(R.drawable.top5);
				holder.awardIV.setVisibility(View.VISIBLE);
			} else if (ms.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top20))) {
				holder.awardIV.setImageResource(R.drawable.top20);
				holder.awardIV.setVisibility(View.VISIBLE);
			}
			holder.speakerNameTV.setText(ms.getSpeakerName());
			holder.dateTimeTV.setText(dateFormatter.format(ms.getStartDateTime().getTime()));
			holder.roomTV.setText(ms.getRoom());
			return convertView;
		}
		
		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
	
	static class DayViewHolder {
		long sessionid;
		TextView sessionTitleTV;
		TextView speakerNameTV;
		TextView dateTimeTV;
		TextView roomTV;
		ImageView awardIV;
	}
	
}
