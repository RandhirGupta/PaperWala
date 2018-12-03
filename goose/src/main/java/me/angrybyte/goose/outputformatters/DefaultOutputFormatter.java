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

package me.angrybyte.goose.outputformatters;

/**
 * User: jim plush Date: 12/19/10
 */

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import me.angrybyte.goose.texthelpers.StopWords;
import me.angrybyte.goose.texthelpers.WordStats;

/**
 * this class will be responsible for taking our top node and stripping out junk we don't want and getting it ready for how we want it
 * presented to the user
 */
public class DefaultOutputFormatter implements OutputFormatter {

    private Element topNode;

    /**
     * Deprecated use {@link #getFormattedText(Element)}
     *
     * @param topNode the top most node to format
     * @return the prepared Element
     */
    @Deprecated
    public Element getFormattedElement(Element topNode) {

        this.topNode = topNode;

        removeNodesWithNegativeScores();

        convertLinksToText();

        replaceTagsWithText();

        removeParagraphsWithFewWords();

        return topNode;

    }

    /**
     * Removes all unnecessary elements and formats the selected text nodes
     *
     * @param topNode the top most node to format
     * @return a formatted string with all HTML removed
     */
    public String getFormattedText(Element topNode) {
        this.topNode = topNode;
        removeNodesWithNegativeScores();

        convertLinksToText();
        replaceTagsWithText();
        removeParagraphsWithFewWords();

        // noinspection deprecation
        return getFormattedText();
    }

    /**
     *  Removes unnecessary elements. Unlike getFormattedText(), it does not remove links and other use full html tags used for formatting.
     * @param topNode
     * @return
     */
    @Override
    public String getFormattedTextForWebView(Element topNode) {
        this.topNode = topNode;
        removeNodesWithNegativeScores();

        removeEmptyParagraphs();

        // noinspection deprecation
        return getFormattedText();
    }

    /**
     * Deprecated use {@link #getFormattedText(Element)} takes an element and turns the P tags into \n\n // todo move this to an output
     * formatter object instead of inline here
     */
    @Deprecated
    public String getFormattedText() {
        StringBuilder sb = new StringBuilder();

        Elements nodes = topNode.getAllElements();
        for (Element e : nodes) {
            if (e.tagName().equals("p")) {
                String text = unescapeHtml(e.text()).trim();
                sb.append(text);
                sb.append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * <p>
     * Un-escapes a string containing entity escapes to a string containing the actual Unicode characters corresponding to the escapes.
     * Supports HTML 4.0 entities.
     * </p>
     * <p/>
     * <p>
     * For example, the string "&amp;lt;Fran&amp;ccedil;ais&amp;gt;" will become "&lt;Fran&ccedil;ais&gt;"
     * </p>
     * <p/>
     * <p>
     * If an entity is unrecognized, it is left alone, and inserted verbatim into the result string. e.g. "&amp;gt;&amp;zzzz;x" will become
     * "&gt;&amp;zzzz;x".
     * </p>
     *
     * @param str the <code>String</code> to unescape, may be null
     * @return a new unescaped <code>String</code>, <code>null</code> if null string input
     **/
    public static String unescapeHtml(String str) {
        if (str == null) {
            return null;
        }
        return Entities.HTML40.unescape(str);
    }

    /**
     * cleans up and converts any nodes that should be considered text into text
     */
    private void convertLinksToText() {
        Elements links = topNode.getElementsByTag("a");
        for (Element item : links) {
            if (item.getElementsByTag("img").size() == 0) {
                TextNode tn = new TextNode(item.text(), topNode.baseUri());
                item.replaceWith(tn);
            }
        }
    }

    /**
     * if there are elements inside our top node that have a negative gravity score, let's give em the boot
     */
    private void removeNodesWithNegativeScores() {
        Elements gravityItems = this.topNode.select("*[gravityScore]");
        for (Element item : gravityItems) {
            int score = Integer.parseInt(item.attr("gravityScore"));
            if (score < 1) {
                item.remove();
            }
        }
    }

    /**
     * replace common tags with just text so we don't have any crazy formatting issues so replace <br>
     * , <i>, <strong>, etc.... with whatever text is inside them
     */
    private void replaceTagsWithText() {

        Elements strongs = topNode.getElementsByTag("strong");
        for (Element item : strongs) {
            TextNode tn = new TextNode(item.text(), topNode.baseUri());
            item.replaceWith(tn);
        }

        Elements bolds = topNode.getElementsByTag("b");
        for (Element item : bolds) {
            TextNode tn = new TextNode(item.text(), topNode.baseUri());
            item.replaceWith(tn);
        }

        Elements italics = topNode.getElementsByTag("i");
        for (Element item : italics) {
            TextNode tn = new TextNode(item.text(), topNode.baseUri());
            item.replaceWith(tn);
        }
    }

    /**
     * Remove paragraphs that have less than x number of words, would indicate that it's some sort of link
     */
    private void removeParagraphsWithFewWords() {
        Elements allNodes = this.topNode.getAllElements();
        for (Element el : allNodes) {
            try {
                // get stop words that appear in each node
                WordStats stopWords = StopWords.getStopWordCount(el.text());

                if (stopWords.getStopWordCount() < 5 && el.getElementsByTag("object").size() == 0
                        && el.getElementsByTag("embed").size() == 0) {
                    el.remove();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Removes empty paragraph
     */
    private void removeEmptyParagraphs() {
        Elements allNodes = this.topNode.getAllElements();
        for (Element el : allNodes) {
            try {
                // get stop words that appear in each node
                WordStats stopWords = StopWords.getStopWordCount(el.text());

                if (el.text().trim().length() < 1 && el.getElementsByTag("object").size() == 0
                        && el.getElementsByTag("embed").size() == 0) {
                    el.remove();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

}
