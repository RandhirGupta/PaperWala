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

package me.angrybyte.goose.images;

import org.jsoup.nodes.Element;

/**
 * User: Jim Plush Date: 12/19/10
 */
public class Image {

    /**
     * Holds the Element node of the image we think is top dog
     */
    private Element topImageNode;

    /**
     * Holds the src of the image
     */
    private String imageSrc;

    /**
     * How confident are we in this image extraction? the most images generally the less confident
     */
    private double confidenceScore = 0.0;

    /**
     * What kind of image extraction was used for this? bestGuess, linkTag, openGraph tags?
     */
    private String imageExtractionType = "";

    /**
     * Stores how many bytes this image is.
     */
    private int bytes;

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    @SuppressWarnings("unused")
    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    @SuppressWarnings("unused")
    public String getImageExtractionType() {
        return imageExtractionType;
    }

    public void setImageExtractionType(String imageExtractionType) {
        this.imageExtractionType = imageExtractionType;
    }

    @SuppressWarnings("unused")
    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    @SuppressWarnings("unused")
    public Element getTopImageNode() {
        return topImageNode;
    }

    public void setTopImageNode(Element topImageNode) {
        this.topImageNode = topImageNode;
    }

}
