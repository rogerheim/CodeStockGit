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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.aremaitch.codestock2010.datadownloader.ScheduleBuilder;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.MiniSession;
import com.aremaitch.codestock2010.repository.Session;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_sessions);

		Log.d("MySessionsActivity", "onStart");
		day1Sessions = new ArrayList<MiniSession>();
		day2Sessions = new ArrayList<MiniSession>();
		
		//	This works when StartActivity is responsible for digging the userid out of pref's or
		//	the scanner.
		//	Userid is passed into the activity via an extra in the Intent.
		Intent i = getIntent();
		userid = i.getLongExtra("userid", 0);
		
		
		getUserSessions();
		
		dateFormatter = new SimpleDateFormat(getString(R.string.standard_where_when_format_string));
		flipper = (ViewFlipper) findViewById(R.id.my_sessions_flipper);
		
		View day1View = findViewById(R.id.my_sessions_day_1);
		View day2View = findViewById(R.id.my_sessions_day_2);
		
		ListView day1LV = (ListView)day1View.findViewById(android.R.id.list);
		ListView day2LV = (ListView)day2View.findViewById(android.R.id.list);
		
//		View day1ViewHeader = LayoutInflater.from(this).inflate(R.layout.my_sessions_listheader_item, null);
//		day1ViewHeader.setTag("day:friday");
//		View day2ViewHeader = LayoutInflater.from(this).inflate(R.layout.my_sessions_listheader_item, null);
//		day2ViewHeader.setTag("day:saturday");
		
		TextView tv1 = (TextView) day1View.findViewById(R.id.my_sessions_header_date);
		TextView tv2 = (TextView) day2View.findViewById(R.id.my_sessions_header_date);
		tv1.setText("Friday");
		tv2.setText("Saturday");
		
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
//		day1LV.addHeaderView(day1ViewHeader);
//		day2LV.addHeaderView(day2ViewHeader);
		day1LV.setOnItemClickListener(this);
		day2LV.setOnItemClickListener(this);
		
		day1LV.setAdapter(new DayAdapter(this, day1Sessions));
		day2LV.setAdapter(new DayAdapter(this, day2Sessions));
		LinearLayout my_sessions_main = (LinearLayout) findViewById(R.id.my_sessions_main);
		my_sessions_main.setOnTouchListener((OnTouchListener) this);
	}
	
	

	
	private void getUserSessions() {
		ScheduleBuilder sb = new ScheduleBuilder(this, 
				getString(R.string.schedule_builder_url),
				getString(R.string.schedule_builder_parameter),
				userid);
		
		//	This list is already sorted chronoloically.
		ArrayList<Long> userSessions = sb.getBuiltSchedule();
		splitUserSessions(userSessions);
	}
	
	/**
	 * Splits array of long session id's into MiniSessions by day.
	 * @param userSessions
	 */
	private void splitUserSessions(ArrayList<Long> userSessions) {
		DataHelper dh = new DataHelper(this);
		
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
		startActivity(new Intent()
			.setAction(getString(R.string.session_details_intent_action))
			.addCategory(Intent.CATEGORY_DEFAULT)
			.putExtra(getString(R.string.session_details_intent_sessionid), id));
	}
}
