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

import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.angrybyte.goose.images.Image;

/**
 * This class represents the extraction of an Article from a website
 */
public class Article {

    /**
     * Holds the title of the web page
     */
    private String title;

    private Date publishDate;

    /**
     * holds the meta description meta tag in the html doc
     */
    private String metaDescription;

    /**
     * holds the clean text after we do strip out everything but the text and wrap it up in a nice package this is the guy you probably
     * want, just pure text
     */
    private String cleanedArticleText;

    /**
     * holds the original unmodified HTML that goose retrieved from the URL
     */
    private String rawHtml;

    /**
     * holds the meta keywords that would in the meta tag of the html doc
     */
    private String metaKeywords;

    /**
     * holds the meta data canonical link that may be place in the meta tags of the html doc
     */
    private String canonicalLink;

    /**
     * holds the domain of where the link came from. http://techcrunch.com/article/testme would be tech crunch.com as the domain
     */
    private String domain;

    /**
     * this represents the jSoup element that we think is the big content dude of this page we can use this node to start grabbing text,
     * images, etc.. around the content
     */
    private Element topNode;

    /**
     * if image extractor is enable this would hold the image we think is the best guess for the web page
     */
    private Image topImage;

    /**
     * holds an array of the image candidates we thought might perhaps we decent images related to the content
     */
    private ArrayList<String> imageCandidates = new ArrayList<>();

    /**
     * holds a list of elements that related to youtube or vimeo movie embeds
     */
    private ArrayList<Element> movies;

    /**
     * holds a list of tags extracted from the article
     */
    private Set<String> tags;

    private Map<String, String> additionalData;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The {@link Date} this {@link Article} was published
     *
     * @return an instance of {@link Date} or <code>null</code> if no date was identified
     */
    @SuppressWarnings("unused")
    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    @SuppressWarnings("unused")
    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String getCanonicalLink() {
        return canonicalLink;
    }

    public void setCanonicalLink(String canonicalLink) {
        this.canonicalLink = canonicalLink;
    }

    @SuppressWarnings("unused")
    public String getDomain() {
        return domain;
    }

    public void setDomain(String urlToParse) {
        String domain = "";

        URL url;
        try {
            url = new URL(urlToParse);
            domain = url.getHost();
        } catch (MalformedURLException ignored) {
        }
        this.domain = domain;
    }

    public Element getTopNode() {
        return topNode;
    }

    public void setTopNode(Element topNode) {
        this.topNode = topNode;
    }

    @SuppressWarnings("unused")
    public ArrayList<Element> getMovies() {
        return movies;
    }

    public void setMovies(ArrayList<Element> movies) {
        this.movies = movies;
    }

    /**
     * The unique set of tags that matched: "a[rel=tag], a[href*=/tag/]".
     *
     * @return the unique set of TAGs extracted from this {@link Article}
     */
    @SuppressWarnings("unused")
    public Set<String> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @SuppressWarnings("unused")
    public ArrayList<String> getImageCandidates() {
        return imageCandidates;
    }

    @SuppressWarnings("unused")
    public void setImageCandidates(ArrayList<String> imageCandidates) {
        this.imageCandidates = imageCandidates;
    }

    public Image getTopImage() {
        return topImage;
    }

    public void setTopImage(Image topImage) {
        this.topImage = topImage;
    }

    public String getCleanedArticleText() {
        return cleanedArticleText;
    }

    public void setCleanedArticleText(String cleanedArticleText) {
        this.cleanedArticleText = cleanedArticleText;
    }

    @SuppressWarnings("unused")
    public String getRawHtml() {
        return rawHtml;
    }

    public void setRawHtml(String rawHtml) {
        this.rawHtml = rawHtml;
    }

    /**
     * A property bucket for consumers of goose to store custom data extractions. This is populated by an implementation of
     * {@link me.angrybyte.goose.extractors.AdditionalDataExtractor} which is executed before document cleansing within
     * {@link ContentExtractor#extractContent}
     *
     * @return a {@link Map Map&lt;String,String&gt;} of property name to property value (represented as a {@link String}.
     */
    @SuppressWarnings("unused")
    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }

}
