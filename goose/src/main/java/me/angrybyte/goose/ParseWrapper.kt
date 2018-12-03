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

package me.angrybyte.goose

import android.annotation.SuppressLint

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * User: jim plush Date: 12/16/10
 */

/**
 * This wrapper class is helpful when you start to multi-thread this bitch You'll be able to see the url clearly that you were processing at the time by viewing
 * the stack dump of this class
 */
class ParseWrapper {

    var status = "notStarted"

    var url: String
    var startTime: String

    fun parse(html: String, url: String): Document {
        this.url = url
        this.status = "Started"
        this.startTime = now()
        val doc: Document
        try {
            doc = Jsoup.parse(html)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        this.status = "Done"
        return doc
    }

    companion object {

        @SuppressLint("SimpleDateFormat") // this is a web scrape, no localization standard
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        fun now(): String {
            val cal = Calendar.getInstance()
            return DATE_FORMAT.format(cal.time)
        }
    }

}
