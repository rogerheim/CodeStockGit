package com.aremaitch.codestock2010;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ViewFlipper;

//HACK: Workaround for bug in Android 2.1 & 2.2 SDK. See http://code.google.com/p/android/issues/detail?id=6191

public class MyViewFlipper extends ViewFlipper {

	public MyViewFlipper(Context context) {
		super(context);
	}
	
	public MyViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		int apiLevel = Integer.parseInt(Build.VERSION.SDK);
		if (apiLevel >= 8) {
			try {
				super.onDetachedFromWindow();
			} catch (IllegalArgumentException e) {
				Log.w("MyViewFlipper", "Android project issue 6191 workaround");
			} finally {
				super.stopFlipping();
			}
		} else {
			super.onDetachedFromWindow();
		}
	}

}
