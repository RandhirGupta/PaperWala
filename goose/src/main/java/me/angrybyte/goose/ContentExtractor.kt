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

import android.util.Log

import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Selector

import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.HashSet

import me.angrybyte.goose.cleaners.DefaultDocumentCleaner
import me.angrybyte.goose.cleaners.DocumentCleaner
import me.angrybyte.goose.images.BestImageGuesser
import me.angrybyte.goose.images.ImageExtractor
import me.angrybyte.goose.network.GooseDownloader
import me.angrybyte.goose.outputformatters.DefaultOutputFormatter
import me.angrybyte.goose.outputformatters.Entities
import me.angrybyte.goose.outputformatters.OutputFormatter
import me.angrybyte.goose.texthelpers.ReplaceSequence
import me.angrybyte.goose.texthelpers.StopWords
import me.angrybyte.goose.texthelpers.StringReplacement
import me.angrybyte.goose.texthelpers.StringSplitter
import me.angrybyte.goose.texthelpers.WordStats
import me.angrybyte.goose.texthelpers.string

/**
 * User: jim plush Date: 12/16/10 a lot of work in this class is based on Arc90's readability code that does content extraction in JS I
 * wasn't able to find a good server side codebase to achieve the same so I started with their base ideas and then built additional metrics
 * on top of it such as looking for clusters of english stop words. Gravity was doing 30+ million links per day with this codebase across a
 * series of crawling servers for a project and it held up well. Our current port is slightly different than this one but I'm working to
 * align them so the goose project gets the love as we continue to move forward.
 *
 *
 * Cougar: God dammit, Mustang! This is Ghost Rider 117. This bogey is all over me. He's got missile lock on me. Do I have permission to
 * fire? Stinger: Do not fire until fired upon...
 */

class ContentExtractor
/**
 * overloaded to accept a custom configuration object
 */
(private val config: Configuration) {

    // sets the default cleaner class to prep the HTML for parsing
    private var documentCleaner: DocumentCleaner? = null
    // the MD5 of the URL we're currently parsing, used to references the images we download to the url so we
    // can more easily clean up resources when we're done with the page.
    private var linkHash: String? = null
    // once we have our topNode then we want to format that guy for output to the user
    private var outputFormatter: OutputFormatter? = null
    private var imageExtractor: ImageExtractor? = null

    private val docCleaner: DocumentCleaner
        get() {
            if (this.documentCleaner == null) {
                this.documentCleaner = DefaultDocumentCleaner()
            }
            return this.documentCleaner
        }

    /**
     * @param urlToCrawl The url you want to extract the text from
     * @param html       If you already have the raw html handy you can pass it here to avoid a network call.
     * @param forWebView true to preserve useful html tags in topNode
     */
    fun extractContent(urlToCrawl: String, html: String, forWebView: Boolean): Article? {
        return performExtraction(urlToCrawl, html, forWebView)
    }

    /**
     * @param urlToCrawl The url you want to extract the text from, makes a network call
     * @param forWebView true to preserve useful html tags in topNode
     */
    fun extractContent(urlToCrawl: String, forWebView: Boolean): Article? {
        return performExtraction(urlToCrawl, null, forWebView)
    }

    private fun performExtraction(urlToCrawl: String, rawHtml: String?, forWebView: Boolean): Article? {
        var urlToCrawl = urlToCrawl
        var rawHtml = rawHtml
        urlToCrawl = getUrlToCrawl(urlToCrawl)
        try {
            URL(urlToCrawl)
            linkHash = md5(urlToCrawl)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("Invalid URL Passed in: $urlToCrawl", e)
        }

        val parseWrapper = ParseWrapper()
        var article: Article? = null
        try {
            if (rawHtml == null) {
                rawHtml = GooseDownloader.getHtml(urlToCrawl, true)
            }

            article = Article()

            article.rawHtml = rawHtml

            var doc = parseWrapper.parse(rawHtml, urlToCrawl)

            // before we cleanse, provide consumers with an opportunity to extract the publish date
            article.publishDate = config.publishDateExtractor!!.extract(doc)

            // now allow for any additional data to be extracted
            article.additionalData = config.additionalDataExtractor.extract(doc)

            // grab the text nodes of any <a ... rel="tag">Tag Name</a> elements
            article.tags = extractTags(doc)

            // now perform a nice deep cleansing
            val documentCleaner = docCleaner
            doc = documentCleaner.clean(doc)

            article.title = getTitle(doc)
            article.metaDescription = getMetaDescription(doc)
            article.metaKeywords = getMetaKeywords(doc)
            article.canonicalLink = getCanonicalLink(doc, urlToCrawl)
            article.domain = article.canonicalLink

            // extract the content of the article
            article.topNode = calculateBestNodeBasedOnClustering(doc)

            if (article.topNode != null) {

                // extract any movie embeds out from our main article content
                article.movies = extractVideos(article.topNode)

                if (config.isEnableImageFetching) {
                    imageExtractor = getImageExtractor(urlToCrawl)
                    article.topImage = imageExtractor!!.getBestImage(doc, article.topNode!!)
                }

                // grab siblings and remove high link density elements
                cleanupNode(article.topNode)
                outputFormatter = getOutputFormatter()

                // if forWebView is enabled, then process topNode such that necessary html tags are not removed.
                if (forWebView)
                    article.cleanedArticleText = outputFormatter!!.getFormattedTextForWebView(article.topNode!!)
                else
                    article.cleanedArticleText = outputFormatter!!.getFormattedText(article.topNode!!)
            }

            // cleans up all the temp images that we've downloaded
            releaseResources()
        } catch (ignored: Exception) {
        }

        return article
    }

    private fun extractTags(node: Element): Set<String> {
        if (node.children().size == 0)
            return NO_STRINGS

        val elements = Selector.select(A_REL_TAG_SELECTOR, node)
        if (elements.size == 0)
            return NO_STRINGS

        val tags = HashSet<String>(elements.size)
        for (el in elements) {
            val tag = el.text()
            if (!string.isNullOrEmpty(tag))
                tags.add(tag)
        }

        return tags
    }

    // used for gawker type ajax sites with pound sites
    private fun getUrlToCrawl(urlToCrawl: String): String {
        val finalURL: String
        if (urlToCrawl.contains("#!")) {
            finalURL = ESCAPED_FRAGMENT_REPLACEMENT.replaceAll(urlToCrawl)
        } else {
            finalURL = urlToCrawl
        }

        return finalURL
    }

    private fun getOutputFormatter(): OutputFormatter {
        return if (outputFormatter == null) {
            DefaultOutputFormatter()
        } else {
            outputFormatter
        }

    }

    private fun getImageExtractor(urlToCrawl: String): ImageExtractor {
        return if (imageExtractor == null) {
            BestImageGuesser(config, urlToCrawl)
        } else {
            imageExtractor
        }

    }

    /**
     * Attempts to grab titles from the html pages, lots of sites use different delimiters for titles so we'll try and do our best guess.
     */
    private fun getTitle(doc: Document): String {
        var title: String? = string.empty

        try {

            val titleElem = doc.getElementsByTag("title")
            if (titleElem == null || titleElem.isEmpty())
                return string.empty

            var titleText = titleElem.first().text()

            if (string.isNullOrEmpty(titleText))
                return string.empty

            var usedDelimiter = false

            if (titleText.contains("|")) {
                titleText = doTitleSplits(titleText, PIPE_SPLITTER)
                usedDelimiter = true
            }

            if (!usedDelimiter && titleText.contains("-")) {
                titleText = doTitleSplits(titleText, DASH_SPLITTER)
                usedDelimiter = true
            }
            if (!usedDelimiter && titleText.contains("»")) {
                titleText = doTitleSplits(titleText, ARROWS_SPLITTER)
                usedDelimiter = true
            }

            if (!usedDelimiter && titleText.contains(":")) {
                titleText = doTitleSplits(titleText, COLON_SPLITTER)
            }

            // encode unicode chars
            title = escapeHtml(titleText)

            // this is a hack until I can fix this.. weird motely crue error with
            // http://money.cnn.com/2010/10/25/news/companies/motley_crue_bp.fortune/index.htm?section=money_latest
            title = MOTLEY_REPLACEMENT.replaceAll(title!!)
        } catch (ignored: NullPointerException) {
        }

        return title
    }

    /**
     * Based on a delimiter in the title take the longest piece or do some custom logic based on the site
     */
    private fun doTitleSplits(title: String, splitter: StringSplitter): String {
        var largetTextLen = 0
        var largeTextIndex = 0

        val titlePieces = splitter.split(title)

        // take the largest split
        for (i in titlePieces.indices) {
            val current = titlePieces[i]
            if (current.length > largetTextLen) {
                largetTextLen = current.length
                largeTextIndex = i
            }
        }

        return TITLE_REPLACEMENTS.replaceAll(titlePieces[largeTextIndex]).trim { it <= ' ' }
    }

    private fun getMetaContent(doc: Document, metaName: String): String {
        val meta = doc.select(metaName)
        if (meta.size > 0) {
            val content = meta.first().attr("content")
            return if (string.isNullOrEmpty(content)) string.empty else content.trim { it <= ' ' }
        }
        return string.empty
    }

    /**
     * If the article has meta description set in the source, use that
     */
    private fun getMetaDescription(doc: Document): String {
        return getMetaContent(doc, "meta[name=description]")
    }

    /**
     * If the article has meta keywords set in the source, use that
     */
    private fun getMetaKeywords(doc: Document): String {
        return getMetaContent(doc, "meta[name=keywords]")
    }

    /**
     * If the article has meta canonical link set in the url
     */
    private fun getCanonicalLink(doc: Document, baseUrl: String): String {
        val meta = doc.select("link[rel=canonical]")
        if (meta.size > 0) {
            val href = meta.first().attr("href")
            return if (string.isNullOrEmpty(href)) string.empty else href.trim { it <= ' ' }
        } else {
            return baseUrl
        }
    }

    private fun getDomain(canonicalLink: String): String {
        try {
            return URL(canonicalLink).host
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }

    }

    /**
     * We're going to start looking for where the clusters of paragraphs are. We'll score a cluster based on the number of stopwords and the
     * number of consecutive paragraphs together, which should form the cluster of text that this node is around also store on how high up
     * the paragraphs are, comments are usually at the bottom and should get a lower score
     */
    private fun calculateBestNodeBasedOnClustering(doc: Document): Element? {
        var topNode: Element? = null

        // grab all the paragraph elements on the page to start to inspect the likely hood of them being good peeps
        val nodesToCheck = getNodesToCheck(doc)

        var startingBoost = 1.0
        var cnt = 0
        var i = 0

        // holds all the parents of the nodes we're checking
        val parentNodes = HashSet<Element>()
        val nodesWithText = ArrayList<Element>()

        for (node in nodesToCheck) {
            val nodeText = node.text()
            val wordStats = StopWords.getStopWordCount(nodeText)
            val highLinkDensity = isHighLinkDensity(node)

            if (wordStats.stopWordCount > 2 && !highLinkDensity) {

                nodesWithText.add(node)
            }
        }

        val numberOfNodes = nodesWithText.size
        val negativeScoring = 0 // we shouldn't give more negatives than positives
        // we want to give the last 20% of nodes negative scores in case they're comments
        val bottomNodesForNegativeScore = numberOfNodes.toFloat() * 0.25

        for (node in nodesWithText) {
            // add parents and grandparents to scoring
            // only add boost to the middle paragraphs, top and bottom is usually jankz city
            // so basically what we're doing is giving boost scores to paragraphs that appear higher up in the dom
            // and giving lower, even negative scores to those who appear lower which could be commenty stuff

            var boostScore = 0f
            if (isOkToBoost(node)) {
                if (cnt >= 0) {
                    boostScore = (1.0 / startingBoost * 50).toFloat()
                    startingBoost++
                }
            }

            // check for negative node values
            if (numberOfNodes > 15) {
                if (numberOfNodes - i <= bottomNodesForNegativeScore) {
                    val booster = bottomNodesForNegativeScore.toFloat() - (numberOfNodes - i).toFloat()
                    boostScore = -Math.pow(booster.toDouble(), 2.toFloat().toDouble()).toFloat()

                    // we don't want to score too highly on the negative side.
                    val negScore = Math.abs(boostScore) + negativeScoring
                    if (negScore > 40) {
                        boostScore = 5f
                    }
                }
            }

            val nodeText = node.text()
            val wordStats = StopWords.getStopWordCount(nodeText)
            val upScore = (wordStats.stopWordCount + boostScore).toInt()
            updateScore(node.parent(), upScore)
            updateScore(node.parent().parent(), upScore / 2)
            updateNodeCount(node.parent(), 1)
            updateNodeCount(node.parent().parent(), 1)

            if (!parentNodes.contains(node.parent())) {
                parentNodes.add(node.parent())
            }

            if (!parentNodes.contains(node.parent().parent())) {
                parentNodes.add(node.parent().parent())
            }

            cnt++
            i++
        }

        // now let's find the parent node who scored the highest

        var topNodeScore = 0
        for (e in parentNodes) {
            val score = getScore(e)
            if (score > topNodeScore) {
                topNode = e
                topNodeScore = score
            }

            if (topNode == null) {
                topNode = e
            }
        }

        return topNode
    }

    /**
     * Returns a list of nodes we want to search on like paragraphs and tables
     */
    private fun getNodesToCheck(doc: Document): ArrayList<Element> {
        val nodesToCheck = ArrayList<Element>()

        nodesToCheck.addAll(doc.getElementsByTag("p"))
        nodesToCheck.addAll(doc.getElementsByTag("pre"))
        nodesToCheck.addAll(doc.getElementsByTag("td"))
        return nodesToCheck

    }

    /**
     * A lot of times the first paragraph might be the caption under an image so we'll want to make sure if we're going to boost a parent
     * node that it should be connected to other paragraphs, at least for the first n paragraphs so we'll want to make sure that the next
     * sibling is a paragraph and has at least some substatial weight to it
     */
    private fun isOkToBoost(node: Element): Boolean {

        var stepsAway = 0

        var sibling: Element? = node.nextElementSibling()
        while (sibling != null) {

            if (sibling.tagName() == "p") {
                if (stepsAway >= 3) {
                    return false
                }

                val paraText = sibling.text()
                val wordStats = StopWords.getStopWordCount(paraText)
                if (wordStats.stopWordCount > 5) {
                    return true
                }

            }

            // increase how far away the next paragraph is from this node
            stepsAway++

            sibling = sibling.nextElementSibling()
        }

        return false
    }

    /**
     * Adds a score to the gravityScore Attribute we put on divs we'll get the current score then add the score we're passing in to the
     * current
     *
     * @param addToScore - the score to add to the node
     */
    private fun updateScore(node: Element, addToScore: Int) {
        var currentScore: Int
        try {
            val scoreString = node.attr("gravityScore")
            currentScore = if (string.isNullOrEmpty(scoreString)) 0 else Integer.parseInt(scoreString)
        } catch (e: NumberFormatException) {
            currentScore = 0
        }

        val newScore = currentScore + addToScore
        node.attr("gravityScore", Integer.toString(newScore))

    }

    /**
     * Stores how many decent nodes are under a parent node
     */
    private fun updateNodeCount(node: Element, addToCount: Int) {
        var currentScore: Int
        try {
            val countString = node.attr("gravityNodes")
            currentScore = if (string.isNullOrEmpty(countString)) 0 else Integer.parseInt(countString)
        } catch (e: NumberFormatException) {
            currentScore = 0
        }

        val newScore = currentScore + addToCount
        node.attr("gravityNodes", Integer.toString(newScore))

    }

    /**
     * Returns the gravityScore as an integer from this node
     */
    private fun getScore(node: Element?): Int {
        if (node == null)
            return 0
        try {
            val grvScoreString = node.attr("gravityScore")
            return if (string.isNullOrEmpty(grvScoreString)) 0 else Integer.parseInt(grvScoreString)
        } catch (e: NumberFormatException) {
            return 0
        }

    }

    /**
     * Pulls out videos we like
     */
    private fun extractVideos(node: Element?): ArrayList<Element> {
        val candidates = ArrayList<Element>()
        val goodMovies = ArrayList<Element>()
        try {

            val embeds = node!!.parent().getElementsByTag("embed")
            for (el in embeds) {
                candidates.add(el)
            }
            val objects = node.parent().getElementsByTag("object")
            for (el in objects) {
                candidates.add(el)
            }

            for (el in candidates) {

                val attrs = el.attributes()

                for ((key, value) in attrs) {
                    try {
                        if ((value.contains("youtube") || value.contains("vimeo")) && key == "src") {
                            goodMovies.add(el)

                        }
                    } catch (ignored: Exception) {
                    }

                }

            }
        } catch (ignored: Exception) {
        }

        return goodMovies
    }

    /**
     * Remove any divs that looks like non-content, clusters of links, or paras with no gusto
     */
    private fun cleanupNode(node: Element?): Element {
        var node = node
        node = addSiblings(node)

        val nodes = node.children()
        for (e in nodes) {
            if (e.tagName() == "p") {
                continue
            }
            val highLinkDensity = isHighLinkDensity(e)
            if (highLinkDensity) {
                e.remove()
                continue
            }

            // now check for word density
            // grab all the paragraphs in the children and remove ones that are too small to matter
            val subParagraphs = e.getElementsByTag("p")

            for (p in subParagraphs) {
                if (p.text().length < 25) {
                    p.remove()
                }
            }

            // now that we've removed shorty paragraphs let's make sure to exclude any first paragraphs that don't have paras as
            // their next siblings to avoid getting img bylines
            // first let's remove any element that now doesn't have any p tags at all
            val subParagraphs2 = e.getElementsByTag("p")
            if (subParagraphs2.size == 0 && e.tagName() != "td") {
                e.remove()
                continue
            }

            //if this node has a decent enough gravityScore we should keep it as well, might be content
            val topNodeScore = getScore(node)
            val currentNodeScore = getScore(e)
            val thresholdScore = (topNodeScore * .08).toFloat()
            if (currentNodeScore < thresholdScore) {
                if (e.tagName() != "td") {
                    e.remove()
                }
            }

        }

        return node
    }

    /**
     * Adds any siblings that may have a decent score to this node
     */
    private fun addSiblings(node: Element?): Element {
        val baselineScoreForSiblingParagraphs = getBaselineScoreForSiblings(node!!)

        var currentSibling: Element? = node.previousElementSibling()
        while (currentSibling != null) {
            if (currentSibling.tagName() == "p") {
                node.child(0).before(currentSibling.outerHtml())
                currentSibling = currentSibling.previousElementSibling()
                continue
            }

            // check for a paragraph embedded in a containing element
            var insertedSiblings = 0
            val potentialParagraphs = currentSibling.getElementsByTag("p")
            if (potentialParagraphs.first() == null) {
                currentSibling = currentSibling.previousElementSibling()
                continue
            }
            for (firstParagraph in potentialParagraphs) {
                val wordStats = StopWords.getStopWordCount(firstParagraph.text())

                val paragraphScore = wordStats.stopWordCount

                if ((baselineScoreForSiblingParagraphs * .30).toFloat() < paragraphScore) {
                    node.child(insertedSiblings).before("<p>" + firstParagraph.text() + "<p>")
                    insertedSiblings++
                }

            }

            currentSibling = currentSibling.previousElementSibling()
        }
        return node

    }

    /**
     * We could have long articles that have tons of paragraphs so if we tried to calculate the base score against the total text score of
     * those paragraphs it would be unfair. So we need to normalize the score based on the average scoring of the paragraphs within the top
     * node. For example if our total score of 10 paragraphs was 1000 but each had an average value of 100 then 100 should be our base.
     */
    private fun getBaselineScoreForSiblings(topNode: Element): Int {
        var base = 100000
        var numberOfParagraphs = 0
        var scoreOfParagraphs = 0

        val nodesToCheck = topNode.getElementsByTag("p")
        for (node in nodesToCheck) {
            val nodeText = node.text()
            val wordStats = StopWords.getStopWordCount(nodeText)
            val highLinkDensity = isHighLinkDensity(node)

            if (wordStats.stopWordCount > 2 && !highLinkDensity) {
                numberOfParagraphs++
                scoreOfParagraphs += wordStats.stopWordCount
            }
        }

        if (numberOfParagraphs > 0) {
            base = scoreOfParagraphs / numberOfParagraphs
        }

        return base
    }

    /**
     * Cleans up any temp files we have laying around like temp images removes any image in the temp dir that starts with the linkHash of
     * the url we parsed
     */
    fun releaseResources() {
        val dir = File(config.cacheDirectory)
        val children = dir.list()

        if (children != null) {
            for (filename in children) {
                if (filename.startsWith(this.linkHash!!)) {
                    val f = File(dir.absolutePath + "/" + filename)
                    if (!f.delete()) {
                        Log.e(ContentExtractor::class.java.name, "Unable to remove temp file: $filename")
                    }
                }
            }
        }
    }

    companion object {

        private val MOTLEY_REPLACEMENT = StringReplacement.compile("&#65533;", string.empty)

        private val ESCAPED_FRAGMENT_REPLACEMENT = StringReplacement.compile("#!", "?_escaped_fragment_=")

        private val TITLE_REPLACEMENTS = ReplaceSequence.create("&raquo;").append("»")
        private val PIPE_SPLITTER = StringSplitter("\\|")
        private val DASH_SPLITTER = StringSplitter(" - ")
        private val ARROWS_SPLITTER = StringSplitter("»")
        private val COLON_SPLITTER = StringSplitter(":")
        private val SPACE_SPLITTER = StringSplitter(" ")

        private val NO_STRINGS = HashSet<String>(0)
        private val A_REL_TAG_SELECTOR = "a[rel=tag], a[href*=/tag/]"

        /**
         * Return a string of 32 lower case hex characters.
         *
         * @return a string of 32 hex characters
         */
        private fun md5(input: String): String {
            val hexHash: String
            try {
                val md = MessageDigest.getInstance("MD5")
                md.update(input.toByteArray())
                val output = md.digest()
                hexHash = bytesToLowerCaseHex(output)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

            return hexHash
        }

        private fun bytesToLowerCaseHex(data: ByteArray): String {
            val buf = StringBuilder()

            for (i in data.indices) {
                var halfByte = data[i].ushr(4) and 0x0F
                var twoHalves = 0
                do {
                    if (0 <= halfByte && halfByte <= 9) {
                        buf.append(('0'.toInt() + halfByte).toChar())
                    } else {
                        buf.append(('a'.toInt() + (halfByte - 10)).toChar())
                    }
                    halfByte = data[i] and 0x0F
                } while (twoHalves++ < 1)
            }
            return buf.toString()
        }

        /**
         *
         *
         * Escapes the characters in a `String` using HTML entities.
         *
         *
         *
         *
         *
         * For example:
         *
         *
         *
         * `"bread" & "butter"`
         *
         * becomes:
         *
         *
         * `&quot;bread&quot; &amp; &quot;butter&quot;`.
         *
         *
         *
         *
         *
         * Supports all known HTML 4.0 entities, including funky accents.
         *
         *
         * @param str the `String` to escape, may be null
         * @return a new escaped `String`, `null` if null string input
         *
         * @see
         * [ISO Entities](http://hotwired.lycos.com/webmonkey/reference/special_characters/)
         *
         * @see
         * [HTML 3.2 Character Entities for ISO Latin-1](http://www.w3.org/TR/REC-html32.latin1)
         *
         * @see
         * [HTML 4.0 Character entity references](http://www.w3.org/TR/REC-html40/sgml/entities.html)
         *
         * @see
         * [HTML 4.01 Character References](http://www.w3.org/TR/html401/charset.html.h-5.3)
         *
         * @see
         * [HTML 4.01 Code positions](http://www.w3.org/TR/html401/charset.html.code-position)
         */
        fun escapeHtml(str: String?): String? {
            return if (str == null) {
                null
            } else Entities.HTML40.escape(str)
            //todo: add a version that takes a Writer
            //todo: rewrite underlying method to use a Writer instead of a StringBuffer
        }

        /**
         * Checks the density of links within a node, is there not much text and most of it contains linky shit? if so it's no good
         */
        private fun isHighLinkDensity(e: Element): Boolean {

            val links = e.getElementsByTag("a")

            if (links.size == 0) {
                return false
            }

            val text = e.text().trim { it <= ' ' }
            val words = SPACE_SPLITTER.split(text)
            val numberOfWords = words.size.toFloat()

            // let's loop through all the links and calculate the number of words that make up the links
            val sb = StringBuilder()
            for (link in links) {
                sb.append(link.text())
            }
            val linkText = sb.toString()
            val linkWords = SPACE_SPLITTER.split(linkText)
            val numberOfLinkWords = linkWords.size.toFloat()

            val numberOfLinks = links.size.toFloat()

            val linkDivisor = numberOfLinkWords / numberOfWords
            val score = linkDivisor * numberOfLinks

            return score > 1
        }
    }

}
