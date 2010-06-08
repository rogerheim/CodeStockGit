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

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		
		
		TextView versiontv = (TextView) this.findViewById(R.id.about_version);
		try {
			PackageManager pmgr = this.getPackageManager();
			PackageInfo pi = pmgr.getPackageInfo(this.getPackageName(), 0);
			
			versiontv.setText("Version " + pi.versionName);
		} catch(Exception e) {
			versiontv.setText("Unable to retrieve version");
		}
	}
}
