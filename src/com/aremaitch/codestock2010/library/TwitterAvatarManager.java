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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.Environment;
import android.text.TextUtils;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.utils.ACLogger;
import twitter4j.*;
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


    public void downloadAvatar(String twitterScreenName, int twitterUserId, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
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

    public void downloadAvatar(String twitterScreenName, int twitterUserId, String srcUrl) {
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
//                    String storedPath = saveImageToCache(twitterUserId, dw);
                    String storedPath = _cacheManager.saveImageToTweetAvatarCache(buildCachedPhotoName(twitterUserId), dw, Bitmap.CompressFormat.PNG);
                    if (!TextUtils.isEmpty(storedPath)) {
                        dh.insertOrUpdateStoredTwitterUrl(twitterUserId, srcUrl, storedPath);
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

    private String buildCachedPhotoName(int twitterUserId) {
        return "T" + String.valueOf(twitterUserId) + ".png";
    }

//    private boolean isAvatarDownloadRequired(int twitterUserId) {
//        //  Users can change their screen name (the userid won't change.)
//        //  Users can change their profile image (the name of the file they uploaded to Twitter
//        //      is part of the downloaded file name.)
//        //  Avatars are stored in avatarCachePath + twitterUserId + actualFileName
//        //  So for userid 123456 and filename my_twitter_image_normal.jpg it would be
//        //      /sdcard/com.aremaitch.codestock2010/twavatarcache/123456/my_twitter_image_normal.jpg
//
//        File imageFile = getCachedPhotoName(twitterUserId);
//        return (!imageFile.exists());
//    }

    private String stripFileName(String longFileName) {
        return longFileName.substring(longFileName.lastIndexOf('/') + 1);
    }

//    private String saveImageToCache(int twitterUserId, Drawable image) {
//        String result = "";
//        File cacheFile = getCachedPhotoName(twitterUserId);
//
//        //  cacheFile is the full path to the file, including the file name.
//        if (cacheFile != null) {
//            BufferedOutputStream buf = null;
//            Bitmap bm = ((BitmapDrawable) image).getBitmap();
//
//            try {
//                buf = new BufferedOutputStream(new FileOutputStream(cacheFile), 8192);
//
//                bm.compress(Bitmap.CompressFormat.PNG, 100, buf);
//                buf.flush();
//                result = cacheFile.getAbsolutePath();
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (buf != null) {
//                    try {
//                        buf.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return result;
//    }


//    private File getCachedPhotoName(int twitterUserID) {
//
//        //  Cached file name is "T" + twitter user id (because the userid won't change while the
//        //  screen name can change.)
//        File cachedFile = null;
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            //  cacheBase is /sdcard/com.aremaitch.codestock2010/twavatarcache
//            cachedFile = new File(getAvatarCachePath(), "T" + String.valueOf(twitterUserID) + ".png");
//        }
//        return cachedFile;
//    }


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


    //  Return the user's avatar from cache. Need the userid and the filename from the url.
    public Drawable getTwitterAvatar(int userId) {
        return _cacheManager.getImageFromTwitterCache(buildCachedPhotoName(userId));
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


    public File[] getListOfAvatarFilesToDelete() {
        return _cacheManager.getTweetAvatarCachePath().listFiles();
    }

//    public File getAvatarCachePath() {
//        return _cacheManager.getTweetAvatarCachePath();
//    }
}
