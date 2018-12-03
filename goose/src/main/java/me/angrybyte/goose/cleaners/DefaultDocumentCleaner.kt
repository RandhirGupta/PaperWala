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

package me.angrybyte.goose.cleaners

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

import me.angrybyte.goose.texthelpers.ReplaceSequence
import me.angrybyte.goose.texthelpers.string

/**
 * User: Jim Plush Date: 12/18/10 This class is used to pre clean documents(webpages) We go through 3 phases of parsing a website cleaning
 * -> extraction -> output formatter This is the cleaning phase that will try to remove comments, known ad junk, social networking divs
 * other things that are known to not be content related.
 */

class DefaultDocumentCleaner : DocumentCleaner {

    override fun clean(doc: Document): Document {
        var docToClean = doc
        docToClean = cleanEmTags(docToClean)
        docToClean = removeDropCaps(docToClean)
        docToClean = removeScriptsAndStyles(docToClean)
        docToClean = cleanBadTags(docToClean)
        docToClean = removeNodesViaRegEx(docToClean, captionPattern)
        docToClean = removeNodesViaRegEx(docToClean, googlePattern)
        docToClean = removeNodesViaRegEx(docToClean, entriesPattern)

        // remove twitter and facebook nodes, mashable has f'd up class names for this
        docToClean = removeNodesViaRegEx(docToClean, facebookPattern)
        docToClean = removeNodesViaRegEx(docToClean, twitterPattern)

        // turn any divs that aren't used as true layout items with block level elements inside them into paragraph tags
        docToClean = convertDivsToParagraphs(docToClean, "div")
        docToClean = convertDivsToParagraphs(docToClean, "span")

        return docToClean
    }

    private fun convertDivsToParagraphs(doc: Document, domType: String): Document {
        val divs = doc.getElementsByTag(domType)
        for (div in divs) {
            try { // this try catches a NPE, so it will just continue when it happens
                val divToPElementsMatcher = divToPElementsPattern.matcher(div.html().toLowerCase())
                if (!divToPElementsMatcher.find()) {
                    val newDoc = Document(doc.baseUri())
                    val newNode = newDoc.createElement("p")

                    newNode.append(div.html())
                    div.replaceWith(newNode)
                } else {
                    // Try to convert any div with just text inside it to a paragraph so it can be counted as text, otherwise it would be ignored
                    // example <div>This is some text in a div</div> should be <div><p>this is some text in a div</p></div>
                    // db(div.childNodes().size() + " childnodes");

                    // create a master text node to hold all the child node texts so that  links that were replaced with text notes
                    // don't become their own paragraphs

                    val replacementText = StringBuilder()
                    val nodesToRemove = ArrayList<Node>()

                    for (kid in div.childNodes()) {
                        if (kid.nodeName() == "#text") {
                            val txtNode = kid as TextNode
                            var text = txtNode.attr("text")
                            if (string.isNullOrEmpty(text)) {
                                continue
                            }

                            //clean up text from tabs and newlines
                            text = tabsAndNewLinesReplcesments.replaceAll(text)

                            if (text.length > 1) {
                                // check for siblings that might be links that we want to include in our new node
                                val previousSib = kid.previousSibling()

                                if (previousSib != null) {
                                    if (previousSib.nodeName() == "a") {
                                        replacementText.append(previousSib.outerHtml())
                                        // Log.d(TAG, "SIBLING NODENAME ADDITION: " + previousSib.nodeName() + " TEXT: " + previousSib.outerHtml());
                                    }
                                }
                                replacementText.append(text)
                                nodesToRemove.add(kid)
                            }
                        }
                    }

                    // replace div's text with the new master replacement text node that containts the sum of all the little text nodes
                    // div.appendChild(replacementTextNode);

                    val newDoc = Document(doc.baseUri())
                    val newPara = newDoc.createElement("p")
                    newPara.html(replacementText.toString())
                    div.childNode(0).before(newPara.outerHtml())

                    for (n in nodesToRemove) {
                        n.remove()
                    }
                }
            } catch (ignored: Exception) {
            }

        }

        return doc
    }

    private fun removeScriptsAndStyles(doc: Document): Document {
        val scripts = doc.getElementsByTag("script")
        for (item in scripts) {
            item.remove()
        }

        val styles = doc.getElementsByTag("style")
        for (style in styles) {
            style.remove()
        }

        return doc
    }

    /**
     * Replaces * tags with text nodes
     * */
    private fun cleanEmTags(doc: Document): Document {
        val ems = doc.getElementsByTag("em")
        for (node in ems) {
            // replace the node with a div node
            val images = node.getElementsByTag("img")
            if (images.size != 0) {
                continue
            }
            val tn = TextNode(node.text(), doc.baseUri())
            node.replaceWith(tn)
        }
        return doc
    }

    /**
     * remove those css drop caps where they put the first letter in big text in the 1st paragraph
     */
    private fun removeDropCaps(doc: Document): Document {
        val items = doc.select("span[class~=(dropcap|drop_cap)]")
        for (item in items) {
            val tn = TextNode(item.text(), doc.baseUri())
            item.replaceWith(tn)
        }
        return doc
    }

    private fun cleanBadTags(doc: Document): Document {
        // only select elements WITHIN the body to avoid removing the body itself
        val children = doc.body().children()

        val naughtyList = children.select(queryNaughtyIDs)
        for (node in naughtyList) {
            removeNode(node)
        }

        // MM remove this?
        // Elements naughtyList2 = children.select(queryNaughtyIDs);
        // for (Element node : naughtyList2) {
        //     removeNode(node);
        // }

        val naughtyList3 = children.select(queryNaughtyClasses)
        for (node in naughtyList3) {
            removeNode(node)
        }

        // MM remove this?
        // Elements naughtyList4 = children.select(queryNaughtyClasses);
        // for (Element node : naughtyList4) {
        //     removeNode(node);
        // }

        // star magazine puts shit on name tags instead of class or id
        val naughtyList5 = children.select(queryNaughtyNames)
        for (node in naughtyList5) {
            removeNode(node)
        }

        return doc
    }

    /**
     * Apparently jSoup expects the node's parent to not be null and throws if it is. Let's be safe.
     *
     * @param node the node to remove from the doc
     */
    private fun removeNode(node: Element?) {
        if (node == null || node.parent() == null)
            return
        node.remove()
    }

    /**
     * removes nodes that may have a certain pattern that matches against a class or id tag
     */
    private fun removeNodesViaRegEx(doc: Document, pattern: Pattern): Document {
        try {
            val naughtyList = doc.getElementsByAttributeValueMatching("id", pattern)
            // Log.d(TAG, "regExRemoveNodes: " + naughtyList.size() + " ID elements found against pattern: " + pattern);
            for (node in naughtyList) {
                removeNode(node)
            }

            val naughtyList3 = doc.getElementsByAttributeValueMatching("class", pattern)
            // Log.d(TAG, "regExRemoveNodes: " + naughtyList3.size() + " CLASS elements found against pattern: " + pattern);
            for (node in naughtyList3) {
                removeNode(node)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            // Log.e(TAG, e.toString());
        }

        return doc
    }

    companion object {

        /*
     * This regex is used to remove undesirable nodes from our doc indicate that something maybe isn't content but more of a comment, footer
     * or some other undesirable node
     */
        private val regExRemoveNodes: String
        private val queryNaughtyIDs: String
        private val queryNaughtyClasses: String
        private val queryNaughtyNames: String

        /**
         * regex to detect if there are block level elements inside of a div element
         */
        private val divToPElementsPattern = Pattern.compile("<(a|blockquote|dl|div|img|ol|p|pre|table|ul)")

        private val tabsAndNewLinesReplcesments: ReplaceSequence
        private val captionPattern = Pattern.compile("^caption$")
        private val googlePattern = Pattern.compile(" google ")
        private val entriesPattern = Pattern.compile("^[^entry-]more.*$")
        private val facebookPattern = Pattern.compile("[^-]facebook")
        private val twitterPattern = Pattern.compile("[^-]twitter")

        init {
            // create negative elements
            regExRemoveNodes = ("^side$|combx|retweet|menucontainer|navbar|comment|PopularQuestions|contact|foot|footer|Footer|footnote|cnn_strycaptiontxt|links|meta$" + "|scroll|shoutbox|sponsor"
                    + "|tags|socialnetworking|socialNetworking|cnnStryHghLght|cnn_stryspcvbx|^inset$|pagetools|post-attributes|welcome_form|contentTools2"
                    + "|the_answers"
                    + "|communitypromo|subscribe|vcard|articleheadings|date|print|popup|author-dropdown|tools|socialtools|byline|konafilter|KonaFilter"
                    + "|breadcrumbs|^fn$|wp-caption-text")
            queryNaughtyIDs = "[id~=($regExRemoveNodes)]"
            queryNaughtyClasses = "[class~=($regExRemoveNodes)]"
            queryNaughtyNames = "[name~=($regExRemoveNodes)]"

            tabsAndNewLinesReplcesments = ReplaceSequence.create("\n", "\n\n").append("\t").append("^\\s+$")
        }
    }

}
