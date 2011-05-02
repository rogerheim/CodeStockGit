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

import java.security.PrivilegedActionException;

import java.text.SimpleDateFormat;
import java.util.*;

import com.aremaitch.codestock2010.library.CSConstants;
import com.aremaitch.codestock2010.library.CSPreferenceManager;
import com.aremaitch.codestock2010.library.CountdownManager;
import com.aremaitch.codestock2010.library.QuickActionMenuManager;
import com.aremaitch.codestock2010.repository.AgendaSession;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.MiniSession;
import com.aremaitch.codestock2010.repository.Session;
import com.aremaitch.utils.ACLogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.flurry.android.FlurryAgent;

public class AgendaActivity extends Activity 
	implements OnTouchListener, AdapterView.OnItemClickListener {


	ViewFlipper flipper;
	private SimpleDateFormat dateFormatter;
	float downXValue;
	int currentView = 0;
	ArrayList<Calendar> sessionStartTimes = new ArrayList<Calendar>();
	int currentSlotIndex = 0;
	int maxSlotIndex = 13;
	
	ListView day0list = null;
	ListView day1list = null;
	
	View view0 = null;
	View view1 = null;
	
	TextView view0header = null;
	TextView view1header = null;


    private CountdownManager cMgr;
    private View digitsContainer;
    private QuickActionMenuManager qaMgr;

    //TODO: Same download options as in SessionTracksActivity
    public static void startMe(Context ctx) {
        //TODO: remove onTouch handling and arrow buttons
        
        Intent i = new Intent(ctx, AgendaActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }
    
	@Override
	public Object onRetainNonConfigurationInstance() {
		ACLogger.info(CSConstants.LOG_TAG, "AgendaActivity onRetainNonConfigurationInstance");
		AgendaActivityInstanceData idata = new AgendaActivityInstanceData();
		idata.currentSlotIndex = this.currentSlotIndex;
		idata.currentView = this.currentView;
		return idata;
	}
	
	//	Idea: If today is Friday (or earlier) start on Friday's view.
	//			If today is Saturday (or later) start on Saturday's view.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda_activity);

		initializeCountdownClock();


		AgendaActivityInstanceData idata = (AgendaActivityInstanceData)getLastNonConfigurationInstance();
		
		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.agenda_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText("");
		
		buildSessionStartTimes();
		
		//	Format for listview header
		dateFormatter = new SimpleDateFormat(getString(R.string.agenda_date_format));
		flipper = (ViewFlipper) findViewById(R.id.agenda_flipper);

		//	Somewhere we need to add the first view to the flipper. This view will be for
		//	the first timeslot of the first day.
		
		//	These are the overall views that are embedded in the flipper. There are only
		//	ever 2 of them (we are actually flipping between two views and changing the
		//	data source.)
		view0 = findViewById(R.id.agenda_view_0);
		view1 = findViewById(R.id.agenda_view_1);
	
		//	These are the headers above each list view. They must be found relative to their
		//	containing view.
		view0header = (TextView)view0.findViewById(R.id.agenda_header_date);
		view1header = (TextView)view1.findViewById(R.id.agenda_header_date);
		
		//	These are the lists themselves. We need them in order to databind them.
		day0list = (ListView)view0.findViewById(android.R.id.list);
		day1list = (ListView)view1.findViewById(android.R.id.list);
		
		ImageButton nextButton0 = (ImageButton)view0.findViewById(R.id.agenda_forward_button);
		nextButton0.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showNextTimeslot();
			}
		});
		
		ImageButton prevButton0 = (ImageButton)view0.findViewById(R.id.agenda_back_button);
		prevButton0.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPreviousTimeslot();
			}
		});
		
		ImageButton nextButton1 = (ImageButton)view1.findViewById(R.id.agenda_forward_button);
		nextButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showNextTimeslot();
			}
		});
		ImageButton prevButton1 = (ImageButton)view1.findViewById(R.id.agenda_back_button);
		prevButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPreviousTimeslot();
			}
		});
		
		//	set onitemclicklistener's and adapters here.
		day0list.setOnItemClickListener(this);
		day1list.setOnItemClickListener(this);
		day0list.setAdapter(new DayAdapter(this, getSessionsInSlot(sessionStartTimes.get(0))));
		day1list.setAdapter(new DayAdapter(this, getSessionsInSlot(sessionStartTimes.get(1))));

		LinearLayout agendaMain = (LinearLayout) findViewById(R.id.agenda_main);
		agendaMain.setOnTouchListener((OnTouchListener) this);
		
		if (idata != null) {
			currentSlotIndex = idata.currentSlotIndex;
			currentView = idata.currentView;
			showTimeSlot();
		} else {
            //  Get preference setting. If set, find first slot and start the display on that.
            if (new CSPreferenceManager(this).isStartAgendaBasedOnDateTimeEnabled()) {
                Calendar targetSlot = findFirstSlotAfterDate(Calendar.getInstance());
                if (targetSlot != null) {
                    currentSlotIndex = sessionStartTimes.indexOf(targetSlot);
                    currentSlotIndex = currentSlotIndex == 0 ? maxSlotIndex : --currentSlotIndex;
                    showNextTimeslot();
                }
            }
        }
	}

    private Calendar findFirstSlotAfterDate(Calendar targetDate) {
        Calendar result = null;
        for (Calendar slot : sessionStartTimes) {
            if (targetDate.before(slot)) {
                result = slot;
                break;
            }
        }
        return result;
    }

    @Override
    protected void onPause() {
        qaMgr.destroyQuickActionMenu();
        stopCountdownClock();
        super.onPause();
    }

    @Override
    protected void onResume() {
        qaMgr = new QuickActionMenuManager(findViewById(R.id.footer_logo));
        qaMgr.initializeQuickActionMenu();
        startCountdownClock();
        super.onResume();
    }

    @Override
    protected void onStart() {
        FlurryAgent.onStartSession(this, getString(R.string.flurry_analytics_api_key));
        super.onStart();
    }

    @Override
    protected void onStop() {
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

    private void initializeCountdownClock() {
        cMgr = new CountdownManager();
        digitsContainer = findViewById(R.id.countdown_digit_container);
    }

    private void startCountdownClock() {
        cMgr.initializeCountdown(digitsContainer, getAssets());
        cMgr.start();
    }

    private void stopCountdownClock() {
        cMgr.stop();
    }

    protected void showNextTimeslot() {

		currentSlotIndex++;
		if (currentSlotIndex > maxSlotIndex)
			currentSlotIndex = 0;

		showTimeSlot();

		flipper.setInAnimation(this, R.anim.slide_left_in);
		flipper.setOutAnimation(this, R.anim.slide_left_out);
		flipper.showNext();

		currentView++;
		if (currentView > 1)
			currentView = 0;
	}

	protected void showPreviousTimeslot() {
		currentSlotIndex--;
		if (currentSlotIndex < 0)
			currentSlotIndex = maxSlotIndex;

		showTimeSlot();

		flipper.setInAnimation(this, R.anim.slide_right_in);
		flipper.setOutAnimation(this, R.anim.slide_right_out);
		flipper.showPrevious();
		currentView--;
		if (currentView < 0)
			currentView = 1;
	}

	void showTimeSlot() {
		DayAdapter da;
		if (currentView == 0) {
			da = (DayAdapter) day1list.getAdapter();
			view1header.setText(dateFormatter.format(sessionStartTimes.get(currentSlotIndex).getTime()));
		} else {
			da = (DayAdapter) day0list.getAdapter();
			view0header.setText(dateFormatter.format(sessionStartTimes.get(currentSlotIndex).getTime()));
		}

		da.setDataSource(getSessionsInSlot(sessionStartTimes.get(currentSlotIndex)));
	}


	private ArrayList<AgendaSession> getSessionsInSlot(Calendar desiredSlot) {
		DataHelper dh = new DataHelper(this);
		ArrayList<AgendaSession> sessions = new ArrayList<AgendaSession>();
		try {
			sessions = dh.getAgendaSessionsInTimeslot(desiredSlot);
		} finally {
			dh.close();
		}
		return sessions;
	}


	//	Refactor to share with MySessionsActivity() ?
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
				showPreviousTimeslot();
			} else if (downXValue > currentX && currentView == 0) {
				showNextTimeslot();
			}
			break;
		}
		}
		return true;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// The 'header' for this view is not part of the ListView.
        DisplaySessionDetailsActivity.startMe(this, id);
//		startActivity(new Intent()
//			.setAction(getString(R.string.session_details_intent_action))
//			.addCategory(Intent.CATEGORY_DEFAULT)
//			.putExtra(CSConstants.SESSION_DETAILS_SESSIONID, id));
	}

	private void buildSessionStartTimes() {
        int year = 2011;
        int month = Calendar.JUNE;
        int friday = 3;
        int saturday = 4;

		//	Bite me!  Months are zero based (Calendar.JANUARY = 0, 6 = Calendar.MAY)
		sessionStartTimes.add(createCalendar(year, month, friday, 8, 30));
		sessionStartTimes.add(createCalendar(year, month, friday, 9, 50));
		sessionStartTimes.add(createCalendar(year, month, friday, 11, 10));
		sessionStartTimes.add(createCalendar(year, month, friday, 12, 30));
		sessionStartTimes.add(createCalendar(year, month, friday, 13, 50));

        //  For 2011, these times are not in the schedule:
        //      3:10 on Friday is Panel Discussion/Opening Circles
        //      Keynote is 6:00PM
//		sessionStartTimes.add(createCalendar(year, month, friday, 15, 10));
//		sessionStartTimes.add(createCalendar(year, month, friday, 17, 45));
		sessionStartTimes.add(createCalendar(year, month, saturday, 8, 30));
		sessionStartTimes.add(createCalendar(year, month, saturday, 9, 50));
		sessionStartTimes.add(createCalendar(year, month, saturday, 11, 10));
		sessionStartTimes.add(createCalendar(year, month, saturday, 12, 30));
		sessionStartTimes.add(createCalendar(year, month, saturday, 13, 50));
		sessionStartTimes.add(createCalendar(year, month, saturday, 15, 10));
		sessionStartTimes.add(createCalendar(year, month, saturday, 16, 30));
        maxSlotIndex = sessionStartTimes.size() - 1;
	}

	private Calendar createCalendar(int year, int month, int day, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, minute, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setTimeZone(TimeZone.getTimeZone("GMT-0400"));
		return cal;
	}
	
	class DayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ArrayList<AgendaSession> daySessions;
		
		public DayAdapter(Context context, ArrayList<AgendaSession> daySessions) {
			mInflater = LayoutInflater.from(context);
			this.daySessions = daySessions;
		}
		
		public void setDataSource(ArrayList<AgendaSession> daySessions) {
			this.daySessions = daySessions;
			notifyDataSetChanged();
			
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

		//TODO: refactor this here and in MySessionsActivity and in SessionTracksActivity.
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SessionViewHolder holder;
			AgendaSession as = daySessions.get(position);
			
			if (convertView == null) {
				
				convertView = mInflater.inflate(R.layout.agenda_item, null);
				holder = new SessionViewHolder();
				holder.awardIV = (ImageView) convertView.findViewById(R.id.agenda_item_session_award);
				holder.roomTV = (TextView) convertView.findViewById(R.id.agenda_item_room);
				holder.sessionTitleTV = (TextView) convertView.findViewById(R.id.agenda_item_session_title);
				holder.speakerNameTV = (TextView) convertView.findViewById(R.id.agenda_item_speaker_name);
				convertView.setTag(holder);
			} else {
				holder = (SessionViewHolder) convertView.getTag();
			}
			holder.sessionid = as.getId();
			holder.sessionTitleTV.setText(as.getSessionTitle());

			if (TextUtils.isEmpty(as.getVoteRank()) || as.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_none))) {
				holder.awardIV.setVisibility(View.INVISIBLE);
			} else if (as.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top1))) {
				holder.awardIV.setImageResource(R.drawable.top1);
				holder.awardIV.setVisibility(View.VISIBLE);
			} else if (as.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top5))) {
				holder.awardIV.setImageResource(R.drawable.top5);
				holder.awardIV.setVisibility(View.VISIBLE);
			} else if (as.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top20))) {
				holder.awardIV.setImageResource(R.drawable.top20);
				holder.awardIV.setVisibility(View.VISIBLE);
			}

			holder.speakerNameTV.setText(as.getSpeakerName());
			holder.roomTV.setText(as.getRoom());

			//	If you change the background color of the item so it's not the background of the list
			//	then Android will not draw the default selector. If you change the listview to 
			//	showSelectorOnTop then it will appear over the item (and it's not transparent so the item
			//	is hidden instead of highlighted.) To fix that you need to define a custom selector but then
			//	you need to deal with 9-patch images so they can be resized properly. Maybe later.
			
//			if (as.getTrackName().toLowerCase().startsWith("developer")) {
//				convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.DevTrack));
//			} else if (as.getTrackName().toLowerCase().startsWith("it pro")) {
//				convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.ITTrack));
//			} else if (as.getTrackName().toLowerCase().startsWith("entrepreneur")) {
//				convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.EntrepreneurTrack));
//			} else if (as.getTrackName().toLowerCase().startsWith("panel")) {
//				convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.PanelTrack));
//			}
			return convertView;
		}
		
	}
	
	static class SessionViewHolder {
		long sessionid;
		TextView roomTV;
		TextView sessionTitleTV;
		TextView speakerNameTV;
		ImageView awardIV;
	}
	
	class AgendaActivityInstanceData {
		public int currentSlotIndex;
		public int currentView;
	}
}
