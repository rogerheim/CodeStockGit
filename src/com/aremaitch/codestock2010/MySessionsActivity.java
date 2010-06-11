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

import java.util.ArrayList;

import com.aremaitch.codestock2010.datadownloader.ScheduleBuilder;
import com.aremaitch.codestock2010.repository.MiniSession;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ViewFlipper;

/*
 * The idea is that the contents of the view is a ViewFlipper. The ViewFlipper
 * contains 2 layouts; each with a TextView and a ListView. The 1st layout is the user's
 * schedule for Friday and the 2nd layout is the user's schdule for Saturday. The user
 * should be able to flip back and forth between the two day by flinging (it is a touchscreen
 * after all.)
 */
public class MySessionsActivity extends Activity {
	ViewFlipper flipper;
	ArrayList<MiniSession> day1Sessions = null;
	ArrayList<MiniSession> day2Sessions = null;
	long userid = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_sessions);

		//	This works when StartActivity is responsible for digging the userid out of pref's or
		//	the scanner.
		//	Userid is passed into the activity via and extra in the Intent.
		Intent i = getIntent();
		userid = i.getLongExtra("userid", 0);
		
		
		getUserSessions();
		
		flipper = (ViewFlipper) findViewById(R.id.my_sessions_flipper);
		
		View day1View = findViewById(R.id.my_sessions_day_1);
		View day2View = findViewById(R.id.my_sessions_day_2);
		
		ListView day1LV = (ListView)day1View.findViewById(android.R.id.list);
		ListView day2LV = (ListView)day2View.findViewById(android.R.id.list);
		
		day1LV.setAdapter(new DayAdapter(this));
		day2LV.setAdapter(new DayAdapter(this));
	}
	
	
	
	private void getUserSessions() {
		ScheduleBuilder sb = new ScheduleBuilder(this, 
				getString(R.string.schedule_builder_url),
				getString(R.string.schedule_builder_parameter),
				userid);
		
		ArrayList<Long> userSessions = sb.getBuiltSchedule();
	}
	
	class DayAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		
		public DayAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
	//	Technique from http://www.codeshogun.com/blog/2009/04/16/how-to-implement-swipe-action-in-android
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
}
