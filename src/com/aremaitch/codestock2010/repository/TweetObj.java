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

import twitter4j.Status;
import twitter4j.Tweet;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/17/11
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */

//  An object to act as a bridge between twitter4j and the database schema.

//  28-Mar-2011: New twitter4j lib uses long instead of int for user id's.
public class TweetObj {
    private String _text;
    private long _toUserId;
    private String _toUser;
    private String _fromUser;
    private long _id;
    private long _fromUserId;
    private String _isoLanguageCode;
    private String _source;
    private String _profileImageUrl;
    private Date _createdAt;
    private double _latitude;
    private double _longitude;

    public static TweetObj createInstance(Tweet rawTweet) {
        TweetObj to = new TweetObj();
        to.setId(rawTweet.getId());
        to.setText(rawTweet.getText());
        to.setToUserId(rawTweet.getToUserId());
        to.setToUser(rawTweet.getToUser());
        to.setFromUser(rawTweet.getFromUser());
        to.setFromUserId(rawTweet.getFromUserId());
        to.setIsoLanguageCode(rawTweet.getIsoLanguageCode());
        to.setProfileImageUrl(rawTweet.getProfileImageUrl());
        to.setSource(rawTweet.getSource());
        to.setCreatedAt(rawTweet.getCreatedAt());
        if (rawTweet.getGeoLocation() != null) {
            to.setLatitude(rawTweet.getGeoLocation().getLatitude());
            to.setLongitude(rawTweet.getGeoLocation().getLongitude());
        }
        return to;
    }

    public static TweetObj createInstance(Status status) {
        TweetObj to = new TweetObj();
        to.setId(status.getId());
        to.setText(status.getText());
        to.setToUserId(status.getInReplyToUserId());
        to.setToUser(status.getInReplyToScreenName());
        if (status.getUser() != null) {
            to.setFromUser(status.getUser().getScreenName());
            to.setFromUserId(status.getUser().getId());
            to.setIsoLanguageCode(status.getUser().getLang());
            to.setProfileImageUrl(status.getUser().getProfileBackgroundImageUrl());
        }
        to.setSource(status.getSource());
        to.setCreatedAt(status.getCreatedAt());
        if (status.getGeoLocation() != null) {
            to.setLatitude(status.getGeoLocation().getLatitude());
            to.setLongitude(status.getGeoLocation().getLongitude());
        }
        return to;

    }

    public String getText() {
        return _text;
    }

    public void setText(String _text) {
        this._text = _text;
    }

    public long getToUserId() {
        return _toUserId;
    }

    public void setToUserId(long _toUserId) {
        this._toUserId = _toUserId;
    }

    public String getToUser() {
        return _toUser;
    }

    public void setToUser(String _toUser) {
        this._toUser = _toUser;
    }

    public String getFromUser() {
        return _fromUser;
    }

    public void setFromUser(String _fromUser) {
        this._fromUser = _fromUser;
    }

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public long getFromUserId() {
        return _fromUserId;
    }

    public void setFromUserId(long _fromUserId) {
        this._fromUserId = _fromUserId;
    }

    public String getIsoLanguageCode() {
        return _isoLanguageCode;
    }

    public void setIsoLanguageCode(String _isoLanguageCode) {
        this._isoLanguageCode = _isoLanguageCode;
    }

    public String getSource() {
        return _source;
    }

    public void setSource(String _source) {
        this._source = _source;
    }

    public String getProfileImageUrl() {
        return _profileImageUrl;
    }

    public void setProfileImageUrl(String _profileImageUrl) {
        this._profileImageUrl = _profileImageUrl;
    }

    public Date getCreatedAt() {
        return _createdAt;
    }

    public void setCreatedAt(Date _createdAt) {
        this._createdAt = _createdAt;
    }

    //  Convenience method to allow passing in a unix time string.
    public void setCreatedAt(long _createdAt) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(_createdAt);
        this._createdAt = cal.getTime();
    }

    public double getLatitude() {
        return _latitude;
    }

    public void setLatitude(double _latitude) {
        this._latitude = _latitude;
    }

    public double getLongitude() {
        return _longitude;
    }

    public void setLongitude(double _longitude) {
        this._longitude = _longitude;
    }
}
