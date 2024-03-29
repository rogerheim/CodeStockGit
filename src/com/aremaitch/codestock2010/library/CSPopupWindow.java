/*
 * Copyright 2010-2011 Roger Heim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aremaitch.codestock2010.library;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.PopupWindow;
import com.aremaitch.codestock2010.R;

/*
   A custom popup window for Twitter-style popup menus.
   Original code from www.londatiga.net.
*/
public class CSPopupWindow {

    protected final View anchor;
    private final PopupWindow window;
    private View root;
    private Drawable background = null;
    protected final WindowManager windowManager;

    public CSPopupWindow(View anchor) {
        this.anchor = anchor;
        this.window = new PopupWindow(anchor.getContext());

        //  When a touch event happens outside of the window, make the window go away.
        window.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    CSPopupWindow.this.dismiss();
                    return true;
                }
                return false;
            }
        });

        windowManager = (WindowManager) anchor.getContext().getSystemService(Context.WINDOW_SERVICE);
        onCreate();
    }

    protected boolean isShowing() {
        return this.window.isShowing();
    }

    protected void showWindowAtLocation(View parent, int gravity, int x, int y) {
        this.window.showAtLocation(parent,  gravity,  x, y);
    }

    protected void setWindowAnimationStyle(int animationStyle) {
        this.window.setAnimationStyle(animationStyle);
    }

    protected void onCreate() {}
    protected void onShow() {}

    protected void preShow() {
        if (root == null) {
            throw new IllegalStateException("setContentView was not called with a view to display");
        }
        onShow();

        if (background == null) {
            window.setBackgroundDrawable(new BitmapDrawable());
        } else {
            window.setBackgroundDrawable(background);
        }

        //  If using the PopupWindow#setBackgroundDrawable these are the only values of the width and height
        //  that make it work. Otherwise you need to set the background of the root viewgroup and set the
        //  popup window background to an empty BitmapDrawable.

        window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setTouchable(true);
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.setContentView(root);
    }

    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        this.background = backgroundDrawable;
    }

    public void setContentView(View root) {
        this.root = root;
        window.setContentView(root);
    }

    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflater.inflate(layoutResID, null));
    }

    /**
     * Set a listener that is called back when the popup window is dismissed.
     * @param listener A {@link PopupWindow.OnDismissListener} that is called when the window is dismissed.
     */
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        window.setOnDismissListener(listener);
    }

    public void showDropDown() {
        showDropDown(0, 0);
    }

    private void showDropDown(int xOffset, int yOffset) {
        preShow();
        window.setAnimationStyle(R.style.Animations_PopDownMenu_Left);
        window.showAsDropDown(anchor, xOffset,  yOffset);
    }

    public void showLikeQuickAction() {
        showLikeQuickAction(0, 0);
    }

    public void showLikeQuickAction(int xOffset, int yOffset) {
        preShow();
        window.setAnimationStyle(R.style.Animations_PopUpMenu_Center);
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int rootWidth = root.getMeasuredWidth();
        int rootHeight = root.getMeasuredHeight();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int xPos = ((screenWidth - rootWidth) / 2) + xOffset;
        int yPos = anchorRect.top - rootHeight + yOffset;

        //  display on bottom
        if (rootHeight > anchorRect.top) {
            yPos = anchorRect.bottom + yOffset;
            window.setAnimationStyle(R.style.Animations_PopDownMenu_Center);
        }
        window.showAtLocation(anchor,  Gravity.NO_GRAVITY, xPos, yPos);
    }

    public void dismiss() {
        window.dismiss();
    }

}
