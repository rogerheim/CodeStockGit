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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;
import com.aremaitch.utils.ACLogger;

//HACK: Workaround for bug in Android 2.1 & 2.2 SDK. See http://code.google.com/p/android/issues/detail?id=6191
// 9-May-2011. It appears this *may* finally be fixed in Honeycomb(!) (it's definately been seen in Gingerbread.)
// However, this code would only check for Froyo or better which means it could still
// crash on Eclair.
// Removed the apilevel check completely.

public class MyViewFlipper extends ViewFlipper {

	public MyViewFlipper(Context context) {
		super(context);
	}
	
	public MyViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            ACLogger.warn("MyViewFlipper", "Android project issue 6191 workaround");
        } finally {
            super.stopFlipping();
        }
	}
}
