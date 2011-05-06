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

package com.aremaitch.utils;

/**
 * Miscellaneous session helper methods that don't really fit anywhere else.
 */
public class SessionUtils {

    /**
     * Method to convert a session title into a link to the website .aspx page.
     *
     * Thank you Mike Neel.
     *
     * @param title The session title.
     * @return A url fragment to the aspx page on the CodeStock website.
     */
    public String buildLinkFromTitle(String title) {
        String allowed = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder link = new StringBuilder();
        link.append("/");

        for (int i = 0; i <= title.length() - 1; i++) {
            String letter = title.substring(i, i + 1).toLowerCase();
            if (allowed.contains(letter)) {
                link.append(letter);
            } else if (link.length() > 0 && !link.substring(link.length() - 1).equalsIgnoreCase("-")) {
                link.append("-");
            }

        }
        if (link.length() > 100) link.setLength(100);

        link.append(".aspx");
        return link.toString();

    }
}
