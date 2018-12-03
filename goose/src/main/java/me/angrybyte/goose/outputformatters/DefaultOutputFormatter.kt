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

package me.angrybyte.goose.outputformatters

/**
 * User: jim plush Date: 12/19/10
 */

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

import me.angrybyte.goose.texthelpers.StopWords
import me.angrybyte.goose.texthelpers.WordStats

/**
 * this class will be responsible for taking our top node and stripping out junk we don't want and getting it ready for how we want it
 * presented to the user
 */
class DefaultOutputFormatter : OutputFormatter {

    private var topNode: Element? = null

    /**
     * Deprecated use [.getFormattedText]
     *
     * @param topNode the top most node to format
     * @return the prepared Element
     */
    @Deprecated("")
    override fun getFormattedElement(topNode: Element): Element {

        this.topNode = topNode

        removeNodesWithNegativeScores()

        convertLinksToText()

        replaceTagsWithText()

        removeParagraphsWithFewWords()

        return topNode

    }

    /**
     * Removes all unnecessary elements and formats the selected text nodes
     *
     * @param topNode the top most node to format
     * @return a formatted string with all HTML removed
     */
    override fun getFormattedText(topNode: Element): String {
        this.topNode = topNode
        removeNodesWithNegativeScores()

        convertLinksToText()
        replaceTagsWithText()
        removeParagraphsWithFewWords()


        return formattedText
    }

    /**
     * Removes unnecessary elements. Unlike getFormattedText(), it does not remove links and other use full html tags used for formatting.
     * @param topNode
     * @return
     */
    override fun getFormattedTextForWebView(topNode: Element): String {
        this.topNode = topNode
        removeNodesWithNegativeScores()

        removeEmptyParagraphs()


        return formattedText
    }

    /**
     * Deprecated use [.getFormattedText] takes an element and turns the P tags into \n\n // todo move this to an output
     * formatter object instead of inline here
     */
    @Deprecated("")
    override fun getFormattedText(): String {
        val sb = StringBuilder()

        val nodes = topNode!!.allElements
        for (e in nodes) {
            if (e.tagName() == "p") {
                val text = unescapeHtml(e.text())!!.trim { it <= ' ' }
                sb.append(text)
                sb.append("\n\n")
            }
        }

        return sb.toString()
    }

    /**
     * cleans up and converts any nodes that should be considered text into text
     */
    private fun convertLinksToText() {
        val links = topNode!!.getElementsByTag("a")
        for (item in links) {
            if (item.getElementsByTag("img").size == 0) {
                val tn = TextNode(item.text(), topNode!!.baseUri())
                item.replaceWith(tn)
            }
        }
    }

    /**
     * if there are elements inside our top node that have a negative gravity score, let's give em the boot
     */
    private fun removeNodesWithNegativeScores() {
        val gravityItems = this.topNode!!.select("*[gravityScore]")
        for (item in gravityItems) {
            val score = Integer.parseInt(item.attr("gravityScore"))
            if (score < 1) {
                item.remove()
            }
        }
    }

    /**
     * replace common tags with just text so we don't have any crazy formatting issues so replace <br></br>
     * , *, **, etc.... with whatever text is inside them
     *** */
    private fun replaceTagsWithText() {

        val strongs = topNode!!.getElementsByTag("strong")
        for (item in strongs) {
            val tn = TextNode(item.text(), topNode!!.baseUri())
            item.replaceWith(tn)
        }

        val bolds = topNode!!.getElementsByTag("b")
        for (item in bolds) {
            val tn = TextNode(item.text(), topNode!!.baseUri())
            item.replaceWith(tn)
        }

        val italics = topNode!!.getElementsByTag("i")
        for (item in italics) {
            val tn = TextNode(item.text(), topNode!!.baseUri())
            item.replaceWith(tn)
        }
    }

    /**
     * Remove paragraphs that have less than x number of words, would indicate that it's some sort of link
     */
    private fun removeParagraphsWithFewWords() {
        val allNodes = this.topNode!!.allElements
        for (el in allNodes) {
            try {
                // get stop words that appear in each node
                val stopWords = StopWords.getStopWordCount(el.text())

                if (stopWords.stopWordCount < 5 && el.getElementsByTag("object").size == 0
                        && el.getElementsByTag("embed").size == 0) {
                    el.remove()
                }
            } catch (ignored: IllegalArgumentException) {
            }

        }
    }

    /**
     * Removes empty paragraph
     */
    private fun removeEmptyParagraphs() {
        val allNodes = this.topNode!!.allElements
        for (el in allNodes) {
            try {
                // get stop words that appear in each node
                val stopWords = StopWords.getStopWordCount(el.text())

                if (el.text().trim { it <= ' ' }.length < 1 && el.getElementsByTag("object").size == 0
                        && el.getElementsByTag("embed").size == 0) {
                    el.remove()
                }
            } catch (ignored: IllegalArgumentException) {
            }

        }
    }

    companion object {

        /**
         *
         *
         * Un-escapes a string containing entity escapes to a string containing the actual Unicode characters corresponding to the escapes.
         * Supports HTML 4.0 entities.
         *
         *
         *
         *
         *
         * For example, the string "&amp;lt;Fran&amp;ccedil;ais&amp;gt;" will become "&lt;Franais&gt;"
         *
         *
         *
         *
         *
         * If an entity is unrecognized, it is left alone, and inserted verbatim into the result string. e.g. "&amp;gt;&amp;zzzz;x" will become
         * "&gt;&amp;zzzz;x".
         *
         *
         * @param str the `String` to unescape, may be null
         * @return a new unescaped `String`, `null` if null string input
         */
        fun unescapeHtml(str: String?): String? {
            return if (str == null) {
                null
            } else Entities.HTML40.unescape(str)
        }
    }

}
