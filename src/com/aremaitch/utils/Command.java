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

/**
 * Created by IntelliJ IDEA.
 * Date: 1/19/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */

//  Command pattern for dialogs that need a response to do something.
//  Android dialog boxes do not wait for a response.
public interface Command {
    public void execute();

    public static final Command NOOP = new Command() {
        public void execute() {}
    };

}
