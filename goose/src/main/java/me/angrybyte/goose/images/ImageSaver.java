/**
 * Licensed to Gravity.com under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Gravity.com licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package me.angrybyte.goose.images;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import me.angrybyte.goose.network.GooseDownloader;

/**
 * This class will be responsible for storing images to disk
 */
public class ImageSaver {

    /**
     * Stores an image to internal storage and returns the name of the file.
     */
    public static String storeTempImage(String linkHash, String imageSrc, String cacheDirectory, int minPicSize) throws Exception {
        FileOutputStream fileStream = null;
        ByteArrayOutputStream webBitmapStream = null;

        try {
            // check the URL, maybe it contains the mime type
            imageSrc = imageSrc.replace(" ", "%20");
            String webType = null;
            try {
                webType = ImageUtils.getFileExtensionSimple(GooseDownloader.getContentInfo(imageSrc, true).mimeType);
            } catch (Exception e) {
                Log.w(ImageSaver.class.getSimpleName(), e.getMessage());
            }
            if (webType == null) {
                webType = "";
            }

            // generate random name
            int randInt = new Random().nextInt();
            String fileName = linkHash + "_" + randInt + webType;
            String fileNameRaw = linkHash + "_" + randInt;
            String filePath = cacheDirectory + File.separator + fileName;
            String filePathRaw = cacheDirectory + File.separator + fileNameRaw;

            // save it to temporary cache
            Bitmap webBitmap = GooseDownloader.getPhoto(imageSrc, true);
            if (webBitmap == null) {
                throw new IllegalArgumentException("Bitmap at " + imageSrc + " doesn't exist");
            }

            fileStream = new FileOutputStream(filePath);
            webBitmapStream = new ByteArrayOutputStream();

            webBitmap.compress(Bitmap.CompressFormat.JPEG, 100, webBitmapStream);
            byte[] byteArray = webBitmapStream.toByteArray();
            fileStream.write(byteArray);
            fileStream.flush();
            close(fileStream);
            close(webBitmapStream);

            // get mime type and store the image extension based on that
            String mimeExtension = ImageUtils.getFileExtension(filePath);
            if (TextUtils.isEmpty(mimeExtension)) {
                return null;
            }

            File f = new File(filePath);
            if (f.length() < minPicSize) {
                return null;
            }

            File newFile = new File(filePathRaw + mimeExtension);
            if (!f.renameTo(newFile)) {
                Log.e(ImageSaver.class.getName(), "Can't rename file");
            }
            return filePathRaw + mimeExtension;
        } finally {
            close(fileStream);
            close(webBitmapStream);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

}
