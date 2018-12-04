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

import android.graphics.BitmapFactory;

import java.io.IOException;
import java.util.Locale;

public class ImageUtils {

    /**
     * Reads the image dimensions.
     */
    public static ImageDetails getImageDetails(String filePath) throws IOException {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // returns null, sizes are in the options variable
            BitmapFactory.decodeFile(filePath, options);
            int width = options.outWidth;
            int height = options.outHeight;
            String type = options.outMimeType;

            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setMimeType(type);
            imageDetails.setWidth(width);
            imageDetails.setHeight(height);
            return imageDetails;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Reads the file extension from the given content type or URL (in String form), {@code null} if not found.
     */
    public static String getFileExtensionSimple(String contentType) {
        if (contentType != null) {
            contentType = contentType.toUpperCase(Locale.getDefault());
            if (contentType.endsWith("GIF")) {
                return ".gif";
            } else if (contentType.endsWith("JPEG") || contentType.endsWith("JPG")) {
                return ".jpg";
            } else if (contentType.endsWith("PNG")) {
                return ".png";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Reads the file extension based on the file's mime type.
     */
    public static String getFileExtension(String filePath) throws IOException, SecretGifException {
        try {
            ImageDetails imageDetails = ImageUtils.getImageDetails(filePath);
            String mimeTypeCaps = imageDetails.getMimeType().toUpperCase();

            if (mimeTypeCaps.contains("GIF")) {
                throw new SecretGifException();
            }

            String fileExtension;
            if (mimeTypeCaps.contains("JPEG") || mimeTypeCaps.contains("JPG")) {
                fileExtension = ".jpg";
            } else if (mimeTypeCaps.contains("PNG")) {
                fileExtension = ".png";
            } else {
                throw new IOException("BAD MIME TYPE: " + mimeTypeCaps + " FILENAME:" + filePath);
            }

            return fileExtension;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
