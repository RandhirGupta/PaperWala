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

import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList
import java.util.Date
import java.util.HashSet

import me.angrybyte.goose.images.Image

/**
 * This class represents the extraction of an Article from a website
 */
class Article {

    /**
     * Holds the title of the web page
     */
    var title: String? = null

    /**
     * The [Date] this [Article] was published
     *
     * @return an instance of [Date] or `null` if no date was identified
     */
    var publishDate: Date? = null

    /**
     * holds the meta description meta tag in the html doc
     */
    var metaDescription: String? = null

    /**
     * holds the clean text after we do strip out everything but the text and wrap it up in a nice package this is the guy you probably
     * want, just pure text
     */
    var cleanedArticleText: String? = null

    /**
     * holds the original unmodified HTML that goose retrieved from the URL
     */
    var rawHtml: String? = null

    /**
     * holds the meta keywords that would in the meta tag of the html doc
     */
    var metaKeywords: String? = null

    /**
     * holds the meta data canonical link that may be place in the meta tags of the html doc
     */
    var canonicalLink: String? = null

    /**
     * holds the domain of where the link came from. http://techcrunch.com/article/testme would be tech crunch.com as the domain
     */
    var domain: String? = null
        set(urlToParse) {
            var domain = ""

            val url: URL
            try {
                url = URL(urlToParse)
                domain = url.host
            } catch (ignored: MalformedURLException) {
            }

            field = domain
        }

    /**
     * this represents the jSoup element that we think is the big content dude of this page we can use this node to start grabbing text,
     * images, etc.. around the content
     */
    var topNode: Element? = null

    /**
     * if image extractor is enable this would hold the image we think is the best guess for the web page
     */
    var topImage: Image? = null

    /**
     * holds an array of the image candidates we thought might perhaps we decent images related to the content
     */
    var imageCandidates = ArrayList<String>()

    /**
     * holds a list of elements that related to youtube or vimeo movie embeds
     */
    var movies: ArrayList<Element>? = null

    /**
     * holds a list of tags extracted from the article
     */
    /**
     * The unique set of tags that matched: "a[rel=tag], a[href*=/tag/]".
     *
     * @return the unique set of TAGs extracted from this [Article]
     */
    var tags: Set<String>? = null
        get() {
            if (field == null) {
                this.tags = HashSet()
            }
            return field
        }

    /**
     * A property bucket for consumers of goose to store custom data extractions. This is populated by an implementation of
     * [me.angrybyte.goose.extractors.AdditionalDataExtractor] which is executed before document cleansing within
     * [ContentExtractor.extractContent]
     *
     * @return a [Map&amp;lt;String,String&amp;gt;][Map] of property name to property value (represented as a [String].
     */
    var additionalData: Map<String, String>? = null

}
