/**
 * Licensed to Gravity.com under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Gravity.com licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package me.angrybyte.goose.images

import android.graphics.BitmapFactory

import java.io.IOException
import java.util.Locale

object ImageUtils {

    /**
     * Reads the image dimensions.
     */
    @Throws(IOException::class)
    fun getImageDetails(filePath: String): ImageDetails {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            // returns null, sizes are in the options variable
            BitmapFactory.decodeFile(filePath, options)
            val width = options.outWidth
            val height = options.outHeight
            val type = options.outMimeType

            val imageDetails = ImageDetails()
            imageDetails.mimeType = type
            imageDetails.width = width
            imageDetails.height = height
            return imageDetails
        } catch (e: Exception) {
            throw IOException(e)
        }

    }

    /**
     * Reads the file extension from the given content type or URL (in String form), `null` if not found.
     */
    fun getFileExtensionSimple(contentType: String?): String? {
        var contentType = contentType
        if (contentType != null) {
            contentType = contentType.toUpperCase(Locale.getDefault())
            return if (contentType.endsWith("GIF")) {
                ".gif"
            } else if (contentType.endsWith("JPEG") || contentType.endsWith("JPG")) {
                ".jpg"
            } else if (contentType.endsWith("PNG")) {
                ".png"
            } else {
                null
            }
        } else {
            return null
        }
    }

    /**
     * Reads the file extension based on the file's mime type.
     */
    @Throws(IOException::class, SecretGifException::class)
    fun getFileExtension(filePath: String): String {
        try {
            val imageDetails = ImageUtils.getImageDetails(filePath)
            val mimeTypeCaps = imageDetails.mimeType!!.toUpperCase()

            if (mimeTypeCaps.contains("GIF")) {
                throw SecretGifException()
            }

            val fileExtension: String
            if (mimeTypeCaps.contains("JPEG") || mimeTypeCaps.contains("JPG")) {
                fileExtension = ".jpg"
            } else if (mimeTypeCaps.contains("PNG")) {
                fileExtension = ".png"
            } else {
                throw IOException("BAD MIME TYPE: $mimeTypeCaps FILENAME:$filePath")
            }

            return fileExtension
        } catch (e: Exception) {
            throw IOException(e)
        }

    }

}
