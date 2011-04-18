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
import android.os.Environment;
import android.text.TextUtils;
import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.utils.ACLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * Date: 2/1/11
 * Time: 10:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSCacheManager {
    //  Manages access to cache directories on external storage
    //  CSCacheManager should have no knowledge of the format of the file names; it should be
    //      strictly concerned with where to store them, where to retrieve them from, and where
    //      to delete them from.

    //  4.18.11: Added code to manage speaker photos (removed from DisplaySessionDetailsActivity class.)

    private Context _ctx;
    private static final File tweetAvatarCachePath = new File(CSConstants.BASE_CACHE_PATH, "twavatarcache");
    private static final File speakerPhotoCachePath = new File(CSConstants.BASE_CACHE_PATH, "speakerphotocache");
    static final int BUFFER_SIZE = 8192;


    public CSCacheManager(Context _ctx) {
        this._ctx = _ctx;
        initializeCacheBase();
    }

    public boolean isCacheReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public void saveSpeakerPhotoToCache(String fileName, InputStream inputStream) {
        File cacheFile = getCachedSpeakerPhotoName(fileName);
        FileOutputStream outputStream = null;
        int totalBytes = 0;

        byte[] data = new byte[BUFFER_SIZE];
        try {
            outputStream = new FileOutputStream(cacheFile);
            ACLogger.info(CSConstants.LOG_TAG, "enter save photo loop");
            int bytesRead = inputStream.read(data, 0, BUFFER_SIZE);
            while (bytesRead > -1) {
                outputStream.write(data, 0, bytesRead);
                totalBytes += bytesRead;
                bytesRead = inputStream.read(data, 0, BUFFER_SIZE);
            }
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String saveImageToTweetAvatarCache(String fileName, Drawable image, Bitmap.CompressFormat format) throws IOException {
        String result = "";
        File cachedFile = null;
        if (isCacheReady()) {
            cachedFile = new File(getTweetAvatarCachePath(), fileName);

            ACLogger.info(CSConstants.LOG_TAG, "saving to cachedFile:" + cachedFile.getAbsolutePath());
            BufferedOutputStream buf = null;
            Bitmap bm = ((BitmapDrawable)image).getBitmap();
            try {
                buf = new BufferedOutputStream(new FileOutputStream(cachedFile), 8192);
                bm.compress(format, 100, buf);
                buf.flush();
                result = cachedFile.getAbsolutePath();
            } finally {
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public Drawable getImageFromTwitterCache(String screenName) {
        Drawable image = null;
        if (isCacheReady()) {
            DataHelper dh = null;
            try {
                dh = new DataHelper(this._ctx);
                String path = dh.getTwitterAvatarPath(screenName);
                if (!TextUtils.isEmpty(path)) {
                    File cachedFile = new File(path);

                    if (cachedFile.exists()) {
                        image = Drawable.createFromPath(cachedFile.getAbsolutePath());
                    }
                }
            } finally {
                if (dh != null) {
                    dh.close();
                }
            }
        }
        return image;
    }

    //  Return full path
    public File getTweetAvatarCachePath() {
        return new File(Environment.getExternalStorageDirectory(), tweetAvatarCachePath.getAbsolutePath());
    }

    public File getSpeakerPhotoCachePath() {
        return new File(Environment.getExternalStorageDirectory(), speakerPhotoCachePath.getAbsolutePath());
    }

    /**
     * Is the passed speaker photo name already cached?
     * @param speakerPhotoName The JPG name of the speaker photo.
     * @return True if the speaker photo is already cached, false otherwise.
     */
    public boolean isSpeakerPhotoCached(String speakerPhotoName) {
        boolean result = false;

        File photo = getCachedSpeakerPhotoName(speakerPhotoName);
        if (photo != null) {
            result = photo.exists();
        }
        return result;
    }

    public Drawable getSpeakerPhotoFromCache(String speakerPhotoName) {
        File photoPath = getCachedSpeakerPhotoName(speakerPhotoName);
        if (photoPath != null) {
            return Drawable.createFromPath(photoPath.getAbsolutePath());
        } else
            return null;
    }

    private File getCachedSpeakerPhotoName(String speakerPhotoName) {
        File cachedFile = null;
        if (isCacheReady()) {
            cachedFile = new File(getSpeakerPhotoCachePath(), speakerPhotoName);
        }
        return cachedFile;
    }

    public boolean deleteTweetAvatar(String fileName) {
        if (isCacheReady()) {
            return deleteTweetAvatar(new File(getTweetAvatarCachePath(), fileName));
        }
        return false;
    }

    public boolean deleteTweetAvatar(File file) {
        //noinspection SimplifiableIfStatement
        if (isCacheReady()) {
            return file.delete();
        }
        return false;
    }

    public ArrayList<File> getListOfTweetAvatars(FilenameFilter filter) {
        File[] files = getTweetAvatarCachePath().listFiles(filter);
        return files == null ? new ArrayList<File>() : new ArrayList<File>(Arrays.asList(files));
    }

    private void initializeCacheBase() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            createCacheDirectoriesIfNecessary();
        }
    }

    private void createCacheDirectoriesIfNecessary() {
        try {
            if (!getTweetAvatarCachePath().exists()) {
                createCacheDirectory(getTweetAvatarCachePath());
            }

            if (!getSpeakerPhotoCachePath().exists()) {
                //  Bug fix: was creating tweet avatar cache twice instead of speaker photo cache
                createCacheDirectory(getSpeakerPhotoCachePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCacheDirectory(File path) throws IOException {
        if (!path.exists()) {
            if (path.mkdirs()) {
                File flag = new File(path, ".nomedia");
                if (!flag.exists()) {
                    if (!flag.createNewFile()) {
                        ACLogger.error(CSConstants.LOG_TAG, "could not create cache directory \"" + path.getAbsolutePath() + "\"");
                    }
                }
            }
        }

    }


}
