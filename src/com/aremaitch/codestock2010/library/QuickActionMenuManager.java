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

import android.view.View;
import android.widget.ImageView;
import com.aremaitch.codestock2010.MapActivity;
import com.aremaitch.codestock2010.R;
import com.aremaitch.codestock2010.SessionTracksActivity;
import com.aremaitch.codestock2010.StartActivity;

public class QuickActionMenuManager {
    private View anchorView;
    private QuickActionMenu qam = null;

    public QuickActionMenuManager(View anchorView) {
        this.anchorView = anchorView;
    }

    public void initializeQuickActionMenu() {

        qam = new QuickActionMenu(anchorView);
        qam.addActionItem(new ActionItem("Home", null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity.startMe(view.getContext());
            }
        }));
        qam.addActionItem(new ActionItem("Map", null, new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                MapActivity.startMe(view.getContext());
            }
        }));
        qam.addActionItem(new ActionItem("Sessions", null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionTracksActivity.startMe(view.getContext());
            }
        }));
        qam.setAnimationStyle(QuickActionMenu.ANIM_AUTO);

        anchorView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                qam.show();
            }
        });

    }

    public void destroyQuickActionMenu() {
        if (qam != null) {
            qam.dismiss();
            anchorView = null;  // release reference to view that is going away
        }
    }
}
