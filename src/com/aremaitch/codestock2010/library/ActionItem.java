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

import android.graphics.drawable.Drawable;
import android.view.View;

public class ActionItem {
    private Drawable icon;
    private String title;
    private View.OnClickListener listener;

    public ActionItem() {
        this("", null, null);
    }

    public ActionItem(Drawable icon) {
        this("", icon, null);
    }

    public ActionItem(String itemTitle, Drawable itemIcon, View.OnClickListener onClickListener) {
        this.title = itemTitle;
        this.icon = itemIcon;
        this.listener = onClickListener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public View.OnClickListener getOnClickListener() {
        return this.listener;
    }
}
