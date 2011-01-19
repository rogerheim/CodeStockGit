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

package com.aremaitch.codestock2010.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/18/11
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterDataHelper {

    private static final String TWEETS_TABLE = "tweets";
    private static final String DELETED_TWEETS_TABLE = "droptweets";
    private static final long MS_IN_ONE_DAY = 86400000;

    protected void dbCreate(ArrayList<StringBuilder> tables, ArrayList<StringBuilder> indexes) {
        tables.add(new StringBuilder()
            .append("create table " + TWEETS_TABLE)
            .append("(id integer primary key,")
            .append("ttext text,")
            .append("touserid integer,")
            .append("touser text,")
            .append("fromuser text,")
            .append("fromuserid integer,")
            .append("isolanguagecode text,")
            .append("source text,")
            .append("profileimageurl text,")
            .append("createdat integer,")
            .append("latitude real,")
            .append("longitude real)"));

        tables.add(new StringBuilder()
            .append("create table " + DELETED_TWEETS_TABLE)
            .append("(id integer primary key)"));

        indexes.add(new StringBuilder()
            .append("create index ix_" + TWEETS_TABLE + "_createdat on " + TWEETS_TABLE)
            .append("(createdat)"));

    }

    protected void dbUpgrade(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TWEETS_TABLE);
        db.execSQL("drop table if exists " + DELETED_TWEETS_TABLE);
    }

    protected long insertTweet(SQLiteDatabase db, TweetObj tObj) {
        ContentValues newRow = new ContentValues();
        newRow.put("id", tObj.getId());
        newRow.put("ttext", tObj.getText());
        newRow.put("touserid", tObj.getToUserId());
        newRow.put("touser", tObj.getToUser());
        newRow.put("fromuser", tObj.getFromUser());
        newRow.put("fromuserid", tObj.getFromUserId());
        newRow.put("isolanguagecode", tObj.getIsoLanguageCode());
        newRow.put("source", tObj.getSource());
        newRow.put("profileimageurl", tObj.getProfileImageUrl());
        newRow.put("createdat", tObj.getCreatedAt().getTime());
        newRow.put("latitude", tObj.getLatitude());
        newRow.put("longitude", tObj.getLongitude());
        return db.insert(TWEETS_TABLE, null, newRow);
    }

    protected void deleteTweet(SQLiteDatabase db, long tweetID) {
        //  Add the deleted tweet to the droptweets table.
        ContentValues newRow = new ContentValues();
        newRow.put("id", tweetID);
        db.insert(DELETED_TWEETS_TABLE, null, newRow);
    }

    protected void cleanUpDeletedTweets(SQLiteDatabase db) {
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + DELETED_TWEETS_TABLE, null);
            if (c.moveToFirst()) {
                long tweetID = c.getLong(c.getColumnIndexOrThrow("id"));
                deleteTweet(db, tweetID);
                deleteFromDropTweet(db, tweetID);
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    private void deleteFromDropTweet(SQLiteDatabase db, long tweetID) {
        db.delete(DELETED_TWEETS_TABLE, "id = ?", new String[] {String.valueOf(tweetID)});

    }

    protected void cleanUpOldTweets(SQLiteDatabase db, int daysToKeep) {
        long oldestTweetToDelete = System.currentTimeMillis() - (daysToKeep * MS_IN_ONE_DAY);
        db.execSQL("delete from " + TWEETS_TABLE + " where createdat < ?", new Long[] {oldestTweetToDelete});
    }

}
