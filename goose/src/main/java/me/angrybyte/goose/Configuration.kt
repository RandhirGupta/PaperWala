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

import org.jsoup.nodes.Element

import java.util.Date

import me.angrybyte.goose.extractors.AdditionalDataExtractor
import me.angrybyte.goose.extractors.PublishDateExtractor

/**
 * Worker configuration
 */
class Configuration(
        /**
         * Where images are stored
         */
        val cacheDirectory: String) {

    /**
     * What's the minimum bytes for an image we'd accept is, a lot of times we want to filter out the author's little images in the
     * beginning of the article
     */
    var minBytesForImages = 4500

    /**
     * Set this to false if you don't care about getting images
     */
    var isEnableImageFetching = true

    var publishDateExtractor: PublishDateExtractor? = object : PublishDateExtractor() {
        override fun extract(rootElement: Element): Date? {
            return null
        }
    }
        @Throws(IllegalArgumentException::class)
        set(extractor) {
            if (extractor == null)
                throw IllegalArgumentException("extractor must not be null!")
            field = extractor
        }

    var additionalDataExtractor: AdditionalDataExtractor = object : AdditionalDataExtractor() {
        override fun extract(rootElement: Element): Map<String, String>? {
            return null
        }
    }

}
