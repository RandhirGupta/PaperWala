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

import java.util.Date;
import java.util.Map;

import me.angrybyte.goose.extractors.AdditionalDataExtractor;
import me.angrybyte.goose.extractors.PublishDateExtractor;

/**
 * Worker configuration
 */
public class Configuration {

    /**
     * What's the minimum bytes for an image we'd accept is, a lot of times we want to filter out the author's little images in the
     * beginning of the article
     */
    private int minBytesForImages = 4500;

    /**
     * Where images are stored
     */
    private final String cacheDirectory;

    /**
     * Set this to false if you don't care about getting images
     */
    private boolean enableImageFetching = true;

    public Configuration(String cacheDirectory) {
        super();
        this.cacheDirectory = cacheDirectory;
    }

    private PublishDateExtractor publishDateExtractor = new PublishDateExtractor() {
        @Override
        public Date extract(Element rootElement) {
            return null;
        }
    };

    public PublishDateExtractor getPublishDateExtractor() {
        return publishDateExtractor;
    }

    @SuppressWarnings("unused")
    public void setPublishDateExtractor(PublishDateExtractor extractor) throws IllegalArgumentException {
        if (extractor == null)
            throw new IllegalArgumentException("extractor must not be null!");
        this.publishDateExtractor = extractor;
    }

    private AdditionalDataExtractor additionalDataExtractor = new AdditionalDataExtractor() {
        @Override
        public Map<String, String> extract(Element rootElement) {
            return null;
        }
    };

    public AdditionalDataExtractor getAdditionalDataExtractor() {
        return additionalDataExtractor;
    }

    @SuppressWarnings("unused")
    public void setAdditionalDataExtractor(AdditionalDataExtractor extractor) {
        additionalDataExtractor = extractor;
    }

    public int getMinBytesForImages() {
        return minBytesForImages;
    }

    @SuppressWarnings("unused")
    public void setMinBytesForImages(int minBytesForImages) {
        this.minBytesForImages = minBytesForImages;
    }

    public boolean isEnableImageFetching() {
        return enableImageFetching;
    }

    @SuppressWarnings("unused")
    public void setEnableImageFetching(boolean enableImageFetching) {
        this.enableImageFetching = enableImageFetching;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

}
