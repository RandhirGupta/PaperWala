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

import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.util.Random

import me.angrybyte.goose.network.GooseDownloader

/**
 * This class will be responsible for storing images to disk
 */
object ImageSaver {

    /**
     * Stores an image to internal storage and returns the name of the file.
     */
    @Throws(Exception::class)
    fun storeTempImage(linkHash: String, imageSrc: String, cacheDirectory: String, minPicSize: Int): String? {
        var imageSrc = imageSrc
        var fileStream: FileOutputStream? = null
        var webBitmapStream: ByteArrayOutputStream? = null

        try {
            // check the URL, maybe it contains the mime type
            imageSrc = imageSrc.replace(" ", "%20")
            var webType: String? = null
            try {
                webType = ImageUtils.getFileExtensionSimple(GooseDownloader.getContentInfo(imageSrc, true).mimeType)
            } catch (e: Exception) {
                Log.w(ImageSaver::class.java.simpleName, e.message)
            }

            if (webType == null) {
                webType = ""
            }

            // generate random name
            val randInt = Random().nextInt()
            val fileName = linkHash + "_" + randInt + webType
            val fileNameRaw = linkHash + "_" + randInt
            val filePath = cacheDirectory + File.separator + fileName
            val filePathRaw = cacheDirectory + File.separator + fileNameRaw

            // save it to temporary cache
            val webBitmap = GooseDownloader.getPhoto(imageSrc, true)
                    ?: throw IllegalArgumentException("Bitmap at $imageSrc doesn't exist")

            fileStream = FileOutputStream(filePath)
            webBitmapStream = ByteArrayOutputStream()

            webBitmap.compress(Bitmap.CompressFormat.JPEG, 100, webBitmapStream)
            val byteArray = webBitmapStream.toByteArray()
            fileStream.write(byteArray)
            fileStream.flush()
            close(fileStream)
            close(webBitmapStream)

            // get mime type and store the image extension based on that
            val mimeExtension = ImageUtils.getFileExtension(filePath)
            if (TextUtils.isEmpty(mimeExtension)) {
                return null
            }

            val f = File(filePath)
            if (f.length() < minPicSize) {
                return null
            }

            val newFile = File(filePathRaw + mimeExtension)
            if (!f.renameTo(newFile)) {
                Log.e(ImageSaver::class.java.name, "Can't rename file")
            }
            return filePathRaw + mimeExtension
        } finally {
            close(fileStream)
            close(webBitmapStream)
        }
    }

    private fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (ignored: Exception) {
            }

        }
    }

}
