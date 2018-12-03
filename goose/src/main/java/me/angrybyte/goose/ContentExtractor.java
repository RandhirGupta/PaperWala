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

package me.angrybyte.goose;

import android.util.Log;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.angrybyte.goose.cleaners.DefaultDocumentCleaner;
import me.angrybyte.goose.cleaners.DocumentCleaner;
import me.angrybyte.goose.images.BestImageGuesser;
import me.angrybyte.goose.images.ImageExtractor;
import me.angrybyte.goose.network.GooseDownloader;
import me.angrybyte.goose.outputformatters.DefaultOutputFormatter;
import me.angrybyte.goose.outputformatters.Entities;
import me.angrybyte.goose.outputformatters.OutputFormatter;
import me.angrybyte.goose.texthelpers.ReplaceSequence;
import me.angrybyte.goose.texthelpers.StopWords;
import me.angrybyte.goose.texthelpers.StringReplacement;
import me.angrybyte.goose.texthelpers.StringSplitter;
import me.angrybyte.goose.texthelpers.WordStats;
import me.angrybyte.goose.texthelpers.string;

/**
 * User: jim plush Date: 12/16/10 a lot of work in this class is based on Arc90's readability code that does content extraction in JS I
 * wasn't able to find a good server side codebase to achieve the same so I started with their base ideas and then built additional metrics
 * on top of it such as looking for clusters of english stop words. Gravity was doing 30+ million links per day with this codebase across a
 * series of crawling servers for a project and it held up well. Our current port is slightly different than this one but I'm working to
 * align them so the goose project gets the love as we continue to move forward.
 * <p/>
 * Cougar: God dammit, Mustang! This is Ghost Rider 117. This bogey is all over me. He's got missile lock on me. Do I have permission to
 * fire? Stinger: Do not fire until fired upon...
 */

public class ContentExtractor {

    private static final StringReplacement MOTLEY_REPLACEMENT = StringReplacement.compile("&#65533;", string.empty);

    private static final StringReplacement ESCAPED_FRAGMENT_REPLACEMENT = StringReplacement.compile("#!", "?_escaped_fragment_=");

    private static final ReplaceSequence TITLE_REPLACEMENTS = ReplaceSequence.create("&raquo;").append("»");
    private static final StringSplitter PIPE_SPLITTER = new StringSplitter("\\|");
    private static final StringSplitter DASH_SPLITTER = new StringSplitter(" - ");
    private static final StringSplitter ARROWS_SPLITTER = new StringSplitter("»");
    private static final StringSplitter COLON_SPLITTER = new StringSplitter(":");
    private static final StringSplitter SPACE_SPLITTER = new StringSplitter(" ");

    private static final Set<String> NO_STRINGS = new HashSet<>(0);
    private static final String A_REL_TAG_SELECTOR = "a[rel=tag], a[href*=/tag/]";

    private Configuration config;

    // sets the default cleaner class to prep the HTML for parsing
    private DocumentCleaner documentCleaner;
    // the MD5 of the URL we're currently parsing, used to references the images we download to the url so we
    // can more easily clean up resources when we're done with the page.
    private String linkHash;
    // once we have our topNode then we want to format that guy for output to the user
    private OutputFormatter outputFormatter;
    private ImageExtractor imageExtractor;

    /**
     * overloaded to accept a custom configuration object
     */
    public ContentExtractor(Configuration config) {
        this.config = config;
    }

    /**
     * @param urlToCrawl The url you want to extract the text from
     * @param html       If you already have the raw html handy you can pass it here to avoid a network call.
     * @param forWebView true to preserve useful html tags in topNode
     */
    public Article extractContent(String urlToCrawl, String html, boolean forWebView) {
        return performExtraction(urlToCrawl, html, forWebView);
    }

    /**
     * @param urlToCrawl The url you want to extract the text from, makes a network call
     * @param forWebView true to preserve useful html tags in topNode
     */
    public Article extractContent(String urlToCrawl, boolean forWebView) {
        return performExtraction(urlToCrawl, null, forWebView);
    }

    private Article performExtraction(String urlToCrawl, String rawHtml, boolean forWebView) {
        urlToCrawl = getUrlToCrawl(urlToCrawl);
        try {
            new URL(urlToCrawl);
            linkHash = md5(urlToCrawl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL Passed in: " + urlToCrawl, e);
        }

        ParseWrapper parseWrapper = new ParseWrapper();
        Article article = null;
        try {
            if (rawHtml == null) {
                rawHtml = GooseDownloader.getHtml(urlToCrawl, true);
            }

            article = new Article();

            article.setRawHtml(rawHtml);

            Document doc = parseWrapper.parse(rawHtml, urlToCrawl);

            // before we cleanse, provide consumers with an opportunity to extract the publish date
            article.setPublishDate(config.getPublishDateExtractor().extract(doc));

            // now allow for any additional data to be extracted
            article.setAdditionalData(config.getAdditionalDataExtractor().extract(doc));

            // grab the text nodes of any <a ... rel="tag">Tag Name</a> elements
            article.setTags(extractTags(doc));

            // now perform a nice deep cleansing
            DocumentCleaner documentCleaner = getDocCleaner();
            doc = documentCleaner.clean(doc);

            article.setTitle(getTitle(doc));
            article.setMetaDescription(getMetaDescription(doc));
            article.setMetaKeywords(getMetaKeywords(doc));
            article.setCanonicalLink(getCanonicalLink(doc, urlToCrawl));
            article.setDomain(article.getCanonicalLink());

            // extract the content of the article
            article.setTopNode(calculateBestNodeBasedOnClustering(doc));

            if (article.getTopNode() != null) {

                // extract any movie embeds out from our main article content
                article.setMovies(extractVideos(article.getTopNode()));

                if (config.isEnableImageFetching()) {
                    imageExtractor = getImageExtractor(urlToCrawl);
                    article.setTopImage(imageExtractor.getBestImage(doc, article.getTopNode()));
                }

                // grab siblings and remove high link density elements
                cleanupNode(article.getTopNode());
                outputFormatter = getOutputFormatter();

                // if forWebView is enabled, then process topNode such that necessary html tags are not removed.
                if(forWebView)
                    article.setCleanedArticleText(outputFormatter.getFormattedTextForWebView(article.getTopNode()));
                else
                    article.setCleanedArticleText(outputFormatter.getFormattedText(article.getTopNode()));
            }

            // cleans up all the temp images that we've downloaded
            releaseResources();
        } catch (Exception ignored) {
        }

        return article;
    }

    /**
     * Return a string of 32 lower case hex characters.
     *
     * @return a string of 32 hex characters
     */
    private static String md5(String input) {
        String hexHash;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] output = md.digest();
            hexHash = bytesToLowerCaseHex(output);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return hexHash;
    }

    private static String bytesToLowerCaseHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < data.length; i++) {
            int halfByte = (data[i] >>> 4) & 0x0F;
            int twoHalves = 0;
            do {
                if ((0 <= halfByte) && (halfByte <= 9)) {
                    buf.append((char) ('0' + halfByte));
                } else {
                    buf.append((char) ('a' + (halfByte - 10)));
                }
                halfByte = data[i] & 0x0F;
            } while (twoHalves++ < 1);
        }
        return buf.toString();
    }

    private Set<String> extractTags(Element node) {
        if (node.children().size() == 0)
            return NO_STRINGS;

        Elements elements = Selector.select(A_REL_TAG_SELECTOR, node);
        if (elements.size() == 0)
            return NO_STRINGS;

        Set<String> tags = new HashSet<>(elements.size());
        for (Element el : elements) {
            String tag = el.text();
            if (!string.isNullOrEmpty(tag))
                tags.add(tag);
        }

        return tags;
    }

    // used for gawker type ajax sites with pound sites
    private String getUrlToCrawl(String urlToCrawl) {
        String finalURL;
        if (urlToCrawl.contains("#!")) {
            finalURL = ESCAPED_FRAGMENT_REPLACEMENT.replaceAll(urlToCrawl);
        } else {
            finalURL = urlToCrawl;
        }

        return finalURL;
    }

    private OutputFormatter getOutputFormatter() {
        if (outputFormatter == null) {
            return new DefaultOutputFormatter();
        } else {
            return outputFormatter;
        }

    }

    private ImageExtractor getImageExtractor(String urlToCrawl) {
        if (imageExtractor == null) {
            return new BestImageGuesser(config, urlToCrawl);
        } else {
            return imageExtractor;
        }

    }

    private DocumentCleaner getDocCleaner() {
        if (this.documentCleaner == null) {
            this.documentCleaner = new DefaultDocumentCleaner();
        }
        return this.documentCleaner;
    }

    /**
     * Attempts to grab titles from the html pages, lots of sites use different delimiters for titles so we'll try and do our best guess.
     */
    private String getTitle(Document doc) {
        String title = string.empty;

        try {

            Elements titleElem = doc.getElementsByTag("title");
            if (titleElem == null || titleElem.isEmpty())
                return string.empty;

            String titleText = titleElem.first().text();

            if (string.isNullOrEmpty(titleText))
                return string.empty;

            boolean usedDelimiter = false;

            if (titleText.contains("|")) {
                titleText = doTitleSplits(titleText, PIPE_SPLITTER);
                usedDelimiter = true;
            }

            if (!usedDelimiter && titleText.contains("-")) {
                titleText = doTitleSplits(titleText, DASH_SPLITTER);
                usedDelimiter = true;
            }
            if (!usedDelimiter && titleText.contains("»")) {
                titleText = doTitleSplits(titleText, ARROWS_SPLITTER);
                usedDelimiter = true;
            }

            if (!usedDelimiter && titleText.contains(":")) {
                titleText = doTitleSplits(titleText, COLON_SPLITTER);
            }

            // encode unicode chars
            title = escapeHtml(titleText);

            // this is a hack until I can fix this.. weird motely crue error with
            // http://money.cnn.com/2010/10/25/news/companies/motley_crue_bp.fortune/index.htm?section=money_latest
            title = MOTLEY_REPLACEMENT.replaceAll(title);
        } catch (NullPointerException ignored) {
        }

        return title;
    }

    /**
     * <p>
     * Escapes the characters in a <code>String</code> using HTML entities.
     * </p>
     * <p/>
     * <p>
     * For example:
     * </p>
     * <p>
     * <code>"bread" & "butter"</code>
     * </p>
     * becomes:
     * <p>
     * <code>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</code>.
     * </p>
     * <p/>
     * <p>
     * Supports all known HTML 4.0 entities, including funky accents.
     * </p>
     *
     * @param str the <code>String</code> to escape, may be null
     * @return a new escaped <code>String</code>, <code>null</code> if null string input
     *
     * @see </br>
     * <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO Entities</a>
     * @see </br>
     * <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character Entities for ISO Latin-1</a>
     * @see </br>
     * <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity references</a>
     * @see </br>
     * <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01 Character References</a>
     * @see </br>
     * <a href="http://www.w3.org/TR/html401/charset.html#code-position">HTML 4.01 Code positions</a>
     **/
    public static String escapeHtml(String str) {
        if (str == null) {
            return null;
        }
        //todo: add a version that takes a Writer
        //todo: rewrite underlying method to use a Writer instead of a StringBuffer
        return Entities.HTML40.escape(str);
    }

    /**
     * Based on a delimiter in the title take the longest piece or do some custom logic based on the site
     */
    private String doTitleSplits(String title, StringSplitter splitter) {
        int largetTextLen = 0;
        int largeTextIndex = 0;

        String[] titlePieces = splitter.split(title);

        // take the largest split
        for (int i = 0; i < titlePieces.length; i++) {
            String current = titlePieces[i];
            if (current.length() > largetTextLen) {
                largetTextLen = current.length();
                largeTextIndex = i;
            }
        }

        return TITLE_REPLACEMENTS.replaceAll(titlePieces[largeTextIndex]).trim();
    }

    private String getMetaContent(Document doc, String metaName) {
        Elements meta = doc.select(metaName);
        if (meta.size() > 0) {
            String content = meta.first().attr("content");
            return string.isNullOrEmpty(content) ? string.empty : content.trim();
        }
        return string.empty;
    }

    /**
     * If the article has meta description set in the source, use that
     */
    private String getMetaDescription(Document doc) {
        return getMetaContent(doc, "meta[name=description]");
    }

    /**
     * If the article has meta keywords set in the source, use that
     */
    private String getMetaKeywords(Document doc) {
        return getMetaContent(doc, "meta[name=keywords]");
    }

    /**
     * If the article has meta canonical link set in the url
     */
    private String getCanonicalLink(Document doc, String baseUrl) {
        Elements meta = doc.select("link[rel=canonical]");
        if (meta.size() > 0) {
            String href = meta.first().attr("href");
            return string.isNullOrEmpty(href) ? string.empty : href.trim();
        } else {
            return baseUrl;
        }
    }

    @SuppressWarnings("unused")
    private String getDomain(String canonicalLink) {
        try {
            return new URL(canonicalLink).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * We're going to start looking for where the clusters of paragraphs are. We'll score a cluster based on the number of stopwords and the
     * number of consecutive paragraphs together, which should form the cluster of text that this node is around also store on how high up
     * the paragraphs are, comments are usually at the bottom and should get a lower score
     */
    private Element calculateBestNodeBasedOnClustering(Document doc) {
        Element topNode = null;

        // grab all the paragraph elements on the page to start to inspect the likely hood of them being good peeps
        ArrayList<Element> nodesToCheck = getNodesToCheck(doc);

        double startingBoost = 1.0;
        int cnt = 0;
        int i = 0;

        // holds all the parents of the nodes we're checking
        Set<Element> parentNodes = new HashSet<>();
        ArrayList<Element> nodesWithText = new ArrayList<>();

        for (Element node : nodesToCheck) {
            String nodeText = node.text();
            WordStats wordStats = StopWords.getStopWordCount(nodeText);
            boolean highLinkDensity = isHighLinkDensity(node);

            if (wordStats.getStopWordCount() > 2 && !highLinkDensity) {

                nodesWithText.add(node);
            }
        }

        int numberOfNodes = nodesWithText.size();
        int negativeScoring = 0; // we shouldn't give more negatives than positives
        // we want to give the last 20% of nodes negative scores in case they're comments
        double bottomNodesForNegativeScore = (float) numberOfNodes * 0.25;

        for (Element node : nodesWithText) {
            // add parents and grandparents to scoring
            // only add boost to the middle paragraphs, top and bottom is usually jankz city
            // so basically what we're doing is giving boost scores to paragraphs that appear higher up in the dom
            // and giving lower, even negative scores to those who appear lower which could be commenty stuff

            float boostScore = 0;
            if (isOkToBoost(node)) {
                if (cnt >= 0) {
                    boostScore = (float) ((1.0 / startingBoost) * 50);
                    startingBoost++;
                }
            }

            // check for negative node values
            if (numberOfNodes > 15) {
                if ((numberOfNodes - i) <= bottomNodesForNegativeScore) {
                    float booster = (float) bottomNodesForNegativeScore - (float) (numberOfNodes - i);
                    boostScore = -(float) Math.pow(booster, (float) 2);

                    // we don't want to score too highly on the negative side.
                    float negScore = Math.abs(boostScore) + negativeScoring;
                    if (negScore > 40) {
                        boostScore = 5;
                    }
                }
            }

            String nodeText = node.text();
            WordStats wordStats = StopWords.getStopWordCount(nodeText);
            int upScore = (int) (wordStats.getStopWordCount() + boostScore);
            updateScore(node.parent(), upScore);
            updateScore(node.parent().parent(), upScore / 2);
            updateNodeCount(node.parent(), 1);
            updateNodeCount(node.parent().parent(), 1);

            if (!parentNodes.contains(node.parent())) {
                parentNodes.add(node.parent());
            }

            if (!parentNodes.contains(node.parent().parent())) {
                parentNodes.add(node.parent().parent());
            }

            cnt++;
            i++;
        }

        // now let's find the parent node who scored the highest

        int topNodeScore = 0;
        for (Element e : parentNodes) {
            int score = getScore(e);
            if (score > topNodeScore) {
                topNode = e;
                topNodeScore = score;
            }

            if (topNode == null) {
                topNode = e;
            }
        }

        return topNode;
    }

    /**
     * Returns a list of nodes we want to search on like paragraphs and tables
     */
    private ArrayList<Element> getNodesToCheck(Document doc) {
        ArrayList<Element> nodesToCheck = new ArrayList<>();

        nodesToCheck.addAll(doc.getElementsByTag("p"));
        nodesToCheck.addAll(doc.getElementsByTag("pre"));
        nodesToCheck.addAll(doc.getElementsByTag("td"));
        return nodesToCheck;

    }

    /**
     * Checks the density of links within a node, is there not much text and most of it contains linky shit? if so it's no good
     */
    private static boolean isHighLinkDensity(Element e) {

        Elements links = e.getElementsByTag("a");

        if (links.size() == 0) {
            return false;
        }

        String text = e.text().trim();
        String[] words = SPACE_SPLITTER.split(text);
        float numberOfWords = words.length;

        // let's loop through all the links and calculate the number of words that make up the links
        StringBuilder sb = new StringBuilder();
        for (Element link : links) {
            sb.append(link.text());
        }
        String linkText = sb.toString();
        String[] linkWords = SPACE_SPLITTER.split(linkText);
        float numberOfLinkWords = linkWords.length;

        float numberOfLinks = links.size();

        float linkDivisor = numberOfLinkWords / numberOfWords;
        float score = linkDivisor * numberOfLinks;

        return score > 1;
    }

    /**
     * A lot of times the first paragraph might be the caption under an image so we'll want to make sure if we're going to boost a parent
     * node that it should be connected to other paragraphs, at least for the first n paragraphs so we'll want to make sure that the next
     * sibling is a paragraph and has at least some substatial weight to it
     */
    private boolean isOkToBoost(Element node) {

        int stepsAway = 0;

        Element sibling = node.nextElementSibling();
        while (sibling != null) {

            if (sibling.tagName().equals("p")) {
                if (stepsAway >= 3) {
                    return false;
                }

                String paraText = sibling.text();
                WordStats wordStats = StopWords.getStopWordCount(paraText);
                if (wordStats.getStopWordCount() > 5) {
                    return true;
                }

            }

            // increase how far away the next paragraph is from this node
            stepsAway++;

            sibling = sibling.nextElementSibling();
        }

        return false;
    }

    /**
     * Adds a score to the gravityScore Attribute we put on divs we'll get the current score then add the score we're passing in to the
     * current
     *
     * @param addToScore - the score to add to the node
     */
    private void updateScore(Element node, int addToScore) {
        int currentScore;
        try {
            String scoreString = node.attr("gravityScore");
            currentScore = string.isNullOrEmpty(scoreString) ? 0 : Integer.parseInt(scoreString);
        } catch (NumberFormatException e) {
            currentScore = 0;
        }
        int newScore = currentScore + addToScore;
        node.attr("gravityScore", Integer.toString(newScore));

    }

    /**
     * Stores how many decent nodes are under a parent node
     */
    private void updateNodeCount(Element node, int addToCount) {
        int currentScore;
        try {
            String countString = node.attr("gravityNodes");
            currentScore = string.isNullOrEmpty(countString) ? 0 : Integer.parseInt(countString);
        } catch (NumberFormatException e) {
            currentScore = 0;
        }
        int newScore = currentScore + addToCount;
        node.attr("gravityNodes", Integer.toString(newScore));

    }

    /**
     * Returns the gravityScore as an integer from this node
     */
    private int getScore(Element node) {
        if (node == null)
            return 0;
        try {
            String grvScoreString = node.attr("gravityScore");
            if (string.isNullOrEmpty(grvScoreString))
                return 0;
            return Integer.parseInt(grvScoreString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Pulls out videos we like
     */
    private ArrayList<Element> extractVideos(Element node) {
        ArrayList<Element> candidates = new ArrayList<>();
        ArrayList<Element> goodMovies = new ArrayList<>();
        try {

            Elements embeds = node.parent().getElementsByTag("embed");
            for (Element el : embeds) {
                candidates.add(el);
            }
            Elements objects = node.parent().getElementsByTag("object");
            for (Element el : objects) {
                candidates.add(el);
            }

            for (Element el : candidates) {

                Attributes attrs = el.attributes();

                for (Attribute a : attrs) {
                    try {
                        if ((a.getValue().contains("youtube") || a.getValue().contains("vimeo")) && a.getKey().equals("src")) {
                            goodMovies.add(el);

                        }
                    } catch (Exception ignored) {
                    }
                }

            }
        } catch (Exception ignored) {
        }

        return goodMovies;
    }

    /**
     * Remove any divs that looks like non-content, clusters of links, or paras with no gusto
     */
    private Element cleanupNode(Element node) {
        node = addSiblings(node);

        Elements nodes = node.children();
        for (Element e : nodes) {
            if (e.tagName().equals("p")) {
                continue;
            }
            boolean highLinkDensity = isHighLinkDensity(e);
            if (highLinkDensity) {
                e.remove();
                continue;
            }

            // now check for word density
            // grab all the paragraphs in the children and remove ones that are too small to matter
            Elements subParagraphs = e.getElementsByTag("p");

            for (Element p : subParagraphs) {
                if (p.text().length() < 25) {
                    p.remove();
                }
            }

            // now that we've removed shorty paragraphs let's make sure to exclude any first paragraphs that don't have paras as
            // their next siblings to avoid getting img bylines
            // first let's remove any element that now doesn't have any p tags at all
            Elements subParagraphs2 = e.getElementsByTag("p");
            if (subParagraphs2.size() == 0 && !e.tagName().equals("td")) {
                e.remove();
                continue;
            }

            //if this node has a decent enough gravityScore we should keep it as well, might be content
            int topNodeScore = getScore(node);
            int currentNodeScore = getScore(e);
            float thresholdScore = (float) (topNodeScore * .08);
            if (currentNodeScore < thresholdScore) {
                if (!e.tagName().equals("td")) {
                    e.remove();
                }
            }

        }

        return node;
    }

    /**
     * Adds any siblings that may have a decent score to this node
     */
    private Element addSiblings(Element node) {
        int baselineScoreForSiblingParagraphs = getBaselineScoreForSiblings(node);

        Element currentSibling = node.previousElementSibling();
        while (currentSibling != null) {
            if (currentSibling.tagName().equals("p")) {
                node.child(0).before(currentSibling.outerHtml());
                currentSibling = currentSibling.previousElementSibling();
                continue;
            }

            // check for a paragraph embedded in a containing element
            int insertedSiblings = 0;
            Elements potentialParagraphs = currentSibling.getElementsByTag("p");
            if (potentialParagraphs.first() == null) {
                currentSibling = currentSibling.previousElementSibling();
                continue;
            }
            for (Element firstParagraph : potentialParagraphs) {
                WordStats wordStats = StopWords.getStopWordCount(firstParagraph.text());

                int paragraphScore = wordStats.getStopWordCount();

                if ((float) (baselineScoreForSiblingParagraphs * .30) < paragraphScore) {
                    node.child(insertedSiblings).before("<p>" + firstParagraph.text() + "<p>");
                    insertedSiblings++;
                }

            }

            currentSibling = currentSibling.previousElementSibling();
        }
        return node;

    }

    /**
     * We could have long articles that have tons of paragraphs so if we tried to calculate the base score against the total text score of
     * those paragraphs it would be unfair. So we need to normalize the score based on the average scoring of the paragraphs within the top
     * node. For example if our total score of 10 paragraphs was 1000 but each had an average value of 100 then 100 should be our base.
     */
    private int getBaselineScoreForSiblings(Element topNode) {
        int base = 100000;
        int numberOfParagraphs = 0;
        int scoreOfParagraphs = 0;

        Elements nodesToCheck = topNode.getElementsByTag("p");
        for (Element node : nodesToCheck) {
            String nodeText = node.text();
            WordStats wordStats = StopWords.getStopWordCount(nodeText);
            boolean highLinkDensity = isHighLinkDensity(node);

            if (wordStats.getStopWordCount() > 2 && !highLinkDensity) {
                numberOfParagraphs++;
                scoreOfParagraphs += wordStats.getStopWordCount();
            }
        }

        if (numberOfParagraphs > 0) {
            base = scoreOfParagraphs / numberOfParagraphs;
        }

        return base;
    }

    /**
     * Cleans up any temp files we have laying around like temp images removes any image in the temp dir that starts with the linkHash of
     * the url we parsed
     */
    public void releaseResources() {
        File dir = new File(config.getCacheDirectory());
        String[] children = dir.list();

        if (children != null) {
            for (String filename : children) {
                if (filename.startsWith(this.linkHash)) {
                    File f = new File(dir.getAbsolutePath() + "/" + filename);
                    if (!f.delete()) {
                        Log.e(ContentExtractor.class.getName(), "Unable to remove temp file: " + filename);
                    }
                }
            }
        }
    }

}
