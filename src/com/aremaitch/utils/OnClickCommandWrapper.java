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

package com.aremaitch.utils;

import android.content.DialogInterface;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/19/11
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnClickCommandWrapper implements DialogInterface.OnClickListener {
    private Command command;

    public OnClickCommandWrapper(Command command) {
        this.command = command;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        command.execute();
    }
}
