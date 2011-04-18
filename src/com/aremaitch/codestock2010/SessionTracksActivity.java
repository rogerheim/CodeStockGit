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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.aremaitch.codestock2010.library.CSConstants;
import com.aremaitch.codestock2010.library.CSPreferenceManager;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.MiniSession;
import com.aremaitch.codestock2010.repository.Track;
import com.aremaitch.utils.ACLogger;

//	Orientation change fires onPause, then onCreate, then onStart.
//	Going to a new activity fires onPause then onStop. On return, onStart will be called
//	  but not onCreate()
//	A different app or returning to home fies onPause when leaving then onStart when
//	returning. It does not fire onCreate again (unless Android totally killed us.)

public class SessionTracksActivity extends ExpandableListActivity {
	
	private static ArrayList<Track> sessionTracks = null;
	static DataHelper dh = null;
    FlingListener flingListener;

//	private static boolean layoutInflated = false;

    public static void startMe(Context ctx) {
        ctx.startActivity(new Intent(ctx, SessionTracksActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createDataHelperIfNeeded();

		//	Orientation changes must re-inflate the layout.
		setContentView(R.layout.sessiontracks_list);

        flingListener = new FlingListener(this);

		TextView headerTitle = (TextView)findViewById(R.id.header_title);
		headerTitle.setText(getString(R.string.session_track_list_header_title));
		TextView headerSubTitle = (TextView)findViewById(R.id.header_subtitle);
		headerSubTitle.setText("");
		
		getExpandableListView().setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

                DisplaySessionDetailsActivity.startMe(SessionTracksActivity.this, id);
//				startActivity(new Intent()
//					.setAction(getString(R.string.session_details_intent_action))
//					.addCategory(getString(R.string.session_details_intent_category))
//					.putExtra(CSConstants.SESSION_DETAILS_SESSIONID, id));
				return true;
			}
		});

        getExpandableListView().setOnTouchListener(flingListener);
        
		//	If you use the 'entries' setting in the xml layout the ListView will render the
		//	list using its default styles. By using 'entries' you aren't associating the item view with the
		//	ListView.
		setListAdapter(new EfficientAdapter(this));
	}
	

	private void createDataHelperIfNeeded() {
		if (dh == null) {
			dh = new DataHelper(this);
		}
		
		if (!dh.isOpen()) {
			dh.openDatabase();
		}
		
		if (sessionTracks == null) {
			sessionTracks = dh.getListOfTracks();
		}
	}
	
	@Override
	protected void onPause() {
		ACLogger.verbose(CSConstants.LOG_TAG, "SessionTracksActivity.onPause");
		super.onPause();
		
		//	Don't close the db onPause() but do close it onStop();
	}
	
	@Override
	protected void onStart() {
		ACLogger.verbose(CSConstants.LOG_TAG, "SessionTracksActivity.onStart");
		super.onStart();

		createDataHelperIfNeeded();

		//	When backing into the activity the expandablelistview is being collapsed.
		//	I think it may be because the layout is being inflated again.
		//	So check a flag; if the flag is not set, inflate the layout and everything that
		//	goes with it. Then set the flag. Next time if the flag is already set, do not
		//	inflate the layout again.
		
		//	This causes a problem; the layout needs to be re-inflated on orientation change.
		//	Otherwise we will get a blank layout.
		//	Can I save the current listview position, then restore it on re-inflation?
		
//		if (!layoutInflated) {
//			layoutInflated = true;
//		}
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//	If switching to a different activity close the database if open.
		if (dh != null && dh.isOpen()) {
			dh.close();
		}
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return flingListener.get_detector().onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        StartActivity.startMe(this);
    }



    private class FlingListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
        Context ctx;
        GestureDetector detector;
        int horizontalMovementThreshold;
        int velocityThreshold;

        FlingListener(Context ctx) {
            this(ctx, null);
        }

        public FlingListener(Context ctx, GestureDetector detector) {
            if (detector == null) {
                detector = new GestureDetector(ctx, this);
            }
            this.ctx = ctx;
            this.detector = detector;
            CSPreferenceManager preferenceManager = new CSPreferenceManager(ctx);
            velocityThreshold = preferenceManager.getFlingVelocityThreshold();
            horizontalMovementThreshold = getWindowManager().getDefaultDisplay().getWidth() / preferenceManager.getFlingDistanceSensitivity();
        }

        @Override
        public boolean onFling(MotionEvent startEvent, MotionEvent endEvent, float velocityX, float velocityY) {
            if (Math.abs(velocityX) >= velocityThreshold) {
                //  Fling left; to to map
                if (startEvent.getX() > endEvent.getX() && startEvent.getX() - endEvent.getX() > horizontalMovementThreshold) {
                    MapActivity.startMe(ctx);
                } else if (startEvent.getX() < endEvent.getX() && endEvent.getX() - startEvent.getX() > horizontalMovementThreshold) {
                    //  Fling right; go back to home
                    StartActivity.startMe(ctx);
                    overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
                }
            }
            return true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return detector.onTouchEvent(motionEvent);
        }

        public GestureDetector get_detector() {
            return detector;
        }
    }

	//	This technique of using a ViewHolder comes from 
	//	<sdk_folder>\platforms\<platform>\samples\ApiDemos\src\com\example\android\apis\view\List14.java
	
	private static class EfficientAdapter extends BaseExpandableListAdapter {
		private LayoutInflater mGroupInflater;
		private LayoutInflater mChildInflater;
		private SimpleDateFormat formatter;
	
		//	We get the inflater once, when this adapter is instantiated.
		public EfficientAdapter(Context context) {
			mGroupInflater = LayoutInflater.from(context);
			mChildInflater = LayoutInflater.from(context);
			formatter  = new SimpleDateFormat(context.getString(R.string.standard_where_when_format_string));
		}
		
		//	Group: each track
		//	Child: each session in the track

		@Override
		public Object getChild(int groupPosition, int childPosition) {
//			ACLogger.verbose(savedContext.getString(R.string.logging_tag), "getChild");
			Track selectedTrack = sessionTracks.get(groupPosition);
			if (selectedTrack.getMiniSessions() == null) {
				return null;
			} else {
				return selectedTrack.getMiniSessions().get(childPosition);
			}
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
//			ACLogger.verbose(savedContext.getString(R.string.logging_tag), "getChildId");
			Track selectedTrack = sessionTracks.get(groupPosition);
			if (selectedTrack.getMiniSessions() == null) {
				return -1;
			} else {
				return selectedTrack.getMiniSessions().get(childPosition).getId();
			}
		}

		
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
//			ACLogger.verbose(savedContext.getString(R.string.logging_tag), "getChildView");
			
			SessionViewHolder holder;
			MiniSession ms = sessionTracks.get(groupPosition).getMiniSessions().get(childPosition);
			
			if (convertView == null) {
				convertView = mChildInflater.inflate(R.layout.sessions_in_track_list_item, null);
				holder = new SessionViewHolder();
				holder.sessionTitleTV = (TextView) convertView.findViewById(R.id.sessionintrack_session_title);
				holder.speakerNameTV = (TextView) convertView.findViewById(R.id.sessionintrack_speaker_name);
				holder.dateTimeTV = (TextView) convertView.findViewById(R.id.sessionintrack_datetime);
				holder.roomTV = (TextView) convertView.findViewById(R.id.sessionintrack_room);
				holder.awardIV = (ImageView) convertView.findViewById(R.id.sessionintrack_award);
				convertView.setTag(holder);
			} else {
				holder = (SessionViewHolder)convertView.getTag();
			}
			holder.sessionid = ms.getId();
			holder.sessionTitleTV.setText(ms.getSessionTitle());
			
			if (ms.getVoteRank() == null || ms.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_none))) {
				holder.awardIV.setVisibility(View.INVISIBLE);
			} else 	if (ms.getVoteRank().equalsIgnoreCase(convertView.getContext().getString(R.string.voterank_top1))) {
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
			
            //TODO: re-enable when schedule is set
//			holder.dateTimeTV.setText(formatter.format(ms.getStartDateTime().getTime()));
            holder.dateTimeTV.setText("TBD");
//			holder.roomTV.setText(ms.getRoom());
            holder.roomTV.setText("TBD");
			return convertView;
		}

		
		@Override
		public int getChildrenCount(int groupPosition) {
			//NOTE: getChildrenCount() is called before onGroupExpanded() fires!
			//		If this returns zero then none of the other getChild* methods fire.
//			ACLogger.verbose(savedContext.getString(R.string.logging_tag), "getChildrenCount");
			Track selectedTrack = sessionTracks.get(groupPosition);
			if (selectedTrack.getMiniSessions() == null) {
				selectedTrack.setMiniSessions(getSessionsInTrack(selectedTrack.getId()));
			}
			return selectedTrack.getMiniSessions().size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return sessionTracks.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return sessionTracks.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return sessionTracks.get(groupPosition).getId();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			
			TrackViewHolder holder;
			
			if (convertView == null) {
				convertView = mGroupInflater.inflate(R.layout.sessiontracks_list_item, null);
				holder = new TrackViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.sessiontrack_track_title);
				convertView.setTag(holder);
			} else {
				holder = (TrackViewHolder) convertView.getTag();
			}
			holder.id = sessionTracks.get(groupPosition).getId();
			holder.text.setText(sessionTracks.get(groupPosition).getTrackTitle());
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			//	ID's are set by the database and do not change nor are reused.
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			//	Children are always selectable; selecting one should go to the next activity to
			//	display session details.
			return true;
		}
		
	
//		@Override
//		public void onGroupExpanded(int groupPosition) {
//			Log.v(savedContext.getString(R.string.logging_tag), "onGroupExpanded");
//
//			Track selectedTrack = sessionTracks.get(groupPosition);
//			
//			if (selectedTrack.getMiniSessions() == null) {
//				Log.v(savedContext.getString(R.string.logging_tag), "onGroupExpanded: need to get miniSessions");
//				selectedTrack.setMiniSessions(dh.getListOfSessions(selectedTrack.getId()));
//			}
//		}

		private List<MiniSession> getSessionsInTrack(long trackid) {
			List<MiniSession> result = null;
			
//			dh.openDatabase();
			result = dh.getListOfMiniSessions(trackid);
			return result;
		}
	}
	
	
	//	There is one field in this class per item in the ListView item View. If this view consisted of
	//	2 ImageView's and 3 TextViews, there would be 2 ImageView fields and 3 TextView fields in this
	//	class.
	static class TrackViewHolder {
		long id;
		TextView text;
	}
	
	static class SessionViewHolder {
		long sessionid;
		TextView sessionTitleTV;
		TextView speakerNameTV;
		TextView dateTimeTV;
		TextView roomTV;
		ImageView awardIV;
	}

}
