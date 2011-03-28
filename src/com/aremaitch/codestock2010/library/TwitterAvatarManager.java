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

package com.aremaitch.codestock2010.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.text.TextUtils;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.utils.ACLogger;
import twitter4j.ProfileImage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * Date: 1/25/11
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */

// 28-Mar-2011  New twitter4j changed user id's from int to long.

public class TwitterAvatarManager {
    //  A class to download Twitter user avatars and cache them to SD card.
    //  TwitterAvatarManager should have no knowledge of where the image files are stored or how
    //      to manipulate them. It should be concerned only with how to download them, name them,
    //          and specify their format.

    private Context _ctx;
    private Twitter t;

    private CSCacheManager _cacheManager;

    public TwitterAvatarManager(Context ctx) {
        _ctx = ctx;
        _cacheManager = new CSCacheManager(_ctx);
    }


    public void downloadAvatar(String twitterScreenName, long twitterUserId, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        //  Fire & forget

        if (!_cacheManager.isCacheReady()) {
            ACLogger.info(CSConstants.LOG_TAG, "could not download twitter picture because sdcard is not mounted");
            return;
        }
        Configuration config = new ConfigurationBuilder()
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .build();
        t = new TwitterFactory(config).getInstance();

        try {
            ProfileImage image = t.getProfileImage(twitterScreenName, ProfileImage.NORMAL);
            String srcUrl = image.getURL();

            downloadAvatar(twitterScreenName, twitterUserId, srcUrl);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

    }

    public void downloadAvatar(String twitterScreenName, long twitterUserId, String srcUrl) {
        if (!_cacheManager.isCacheReady()) {
            ACLogger.info(CSConstants.LOG_TAG, "could not download twitter picture because sdcard is not mounted");
            return;
        }

        DataHelper dh = new DataHelper(_ctx);
        String storedUrl = dh.getStoredTwitterUrl(twitterUserId);
        if (TextUtils.isEmpty(storedUrl) || !storedUrl.equals(srcUrl)) {
            //  We either don't have the picture or it changed. Need to download it.
            try {
                URL theUrl = new URL(srcUrl);
                Drawable dw = downloadFromURL(theUrl);
                if (dw != null) {
                    String storedPath = _cacheManager.saveImageToTweetAvatarCache(buildCachedPhotoName(twitterUserId), dw, Bitmap.CompressFormat.PNG);
                    if (!TextUtils.isEmpty(storedPath)) {
                        dh.insertOrUpdateStoredTwitterUrl(twitterUserId, twitterScreenName, srcUrl, storedPath);
                    }
                } else {
                    ACLogger.error(CSConstants.LOG_TAG, "could not download twitter avatar");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        dh.close();
    }

    private String buildCachedPhotoName(long twitterUserId) {
        return "T" + String.valueOf(twitterUserId) + ".png";
    }


    private String stripFileName(String longFileName) {
        return longFileName.substring(longFileName.lastIndexOf('/') + 1);
    }



    private Drawable downloadFromURL(URL url) {
        Drawable image = null;
        HttpURLConnection cn = null;
        InputStream is = null;
        try {
            cn = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(cn.getInputStream(), 8192);
            ACLogger.info(CSConstants.LOG_TAG, "downloading image from " + url.toString());
            image = Drawable.createFromStream(is, "twitter_avatar");
            if (image == null) {
                ACLogger.error(CSConstants.LOG_TAG, "aack! image is null!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }


   
    public Drawable getTwitterAvatar(String screenName) {
        return _cacheManager.getImageFromTwitterCache(screenName);
    }

    public void nukeAllAvatars() {

        ArrayList<File> files = _cacheManager.getListOfTweetAvatars(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (Debug.isDebuggerConnected()) {
                    ACLogger.info(CSConstants.LOG_TAG, "FilenameFilter name=\"" + name + "\"");
                }
                //  substring() in java is different than C#.
                //  In C# it's startPosition, length
                //  In java it's startPosition, endPosition - 1
                return name.substring(name.lastIndexOf("."), name.length()).equalsIgnoreCase(".png");

            }
        });


        if (files != null) {
            for (File f : files) {
                if (!_cacheManager.deleteTweetAvatar(f)) {
                    ACLogger.error(CSConstants.LOG_TAG, "could not delete user avatar '" + f.getAbsolutePath() + "'");
                }
            }
        }
    }
}
