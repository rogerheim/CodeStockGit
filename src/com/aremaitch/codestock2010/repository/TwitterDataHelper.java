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
import com.aremaitch.codestock2010.library.CSConstants;
import com.aremaitch.utils.ACLogger;

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

    /**
     * Get the next tweet in id sequence
     * @param db The SQLiteDatabase to query.
     * @param lastTweetID The id of the last tweet re returned.
     * @return A TweetObj object containing the tweet or null if there is none.
     */
    protected TweetObj getNextTweet(SQLiteDatabase db, long lastTweetID) {
        TweetObj result = null;
        Cursor c = null;
        try {
            c = getNextOrFirstTweet(db, lastTweetID);
            if (c != null) {
                result = new TweetObj();
                result.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                result.setCreatedAt(c.getLong(c.getColumnIndexOrThrow("createdat")));
                result.setText(c.getString(c.getColumnIndexOrThrow("ttext")));
                result.setToUser(c.getString(c.getColumnIndexOrThrow("touser")));
                result.setToUserId(c.getInt(c.getColumnIndexOrThrow("touserid")));
                result.setFromUser(c.getString(c.getColumnIndexOrThrow("fromuser")));
                result.setFromUserId(c.getInt(c.getColumnIndexOrThrow("fromuserid")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
//                ACLogger.info(CSConstants.LOG_TAG, "closing cursor");

                c.close();
            }
        }
        return result;
    }

    private Cursor getNextOrFirstTweet(SQLiteDatabase db, long lastTweetID) {
        Cursor c = null;
        c = db.rawQuery("select id, createdat, ttext, touser, touserid, fromuser, fromuserid from tweets where id > ? order by id limit 1",
                new String[] {Long.toString(lastTweetID)});
        if (!c.moveToFirst()) {
            c.close();      // need to close this cursor before reusing it. otherwise, we will leak.
            // There was no tweet newer than the last one we retrieved.
            // Retrieve the first one on file.
            c = db.rawQuery("select id, createdat, ttext, touser, touserid, fromuser, fromuserid from tweets order by id limit 1", null);
            if (!c.moveToFirst()) {
                c.close();
                c = null;
            }
        }

        return c;
    }
}
