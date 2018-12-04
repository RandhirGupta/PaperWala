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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.angrybyte.goose.Configuration;
import me.angrybyte.goose.network.GooseDownloader;
import me.angrybyte.goose.texthelpers.string;

/**
 * This image extractor will attempt to find the best image nearest the article. Unfortunately this is a slow process since we're actually
 * downloading the image itself to inspect it's actual height/width and area metrics since most of the time these aren't in the image tags
 * themselves or can be falsified. We'll weight the images in descending order depending on how high up they are compared to the top node
 * content
 */
public class BestImageGuesser implements ImageExtractor {

    /**
     * This lists all the known bad button names that we have
     */
    private static final Matcher matchBadImageNames;

    static {
        String negatives = (".html|.gif|.ico|button|twitter.jpg|facebook.jpg|digg.jpg|digg.png|delicious.png|facebook.png|reddit" + ""
                + ".jpg|doubleclick|diggthis|diggThis|adserver|/ads/|ec.atdmt.com") + "|mediaplex.com|adsatt|view.atdmt";
        // create negative elements
        matchBadImageNames = Pattern.compile(negatives).matcher(string.empty);
    }

    /**
     * holds the document that we're extracting the image from
     */
    Document doc;

    /**
     * holds the result of our image extraction
     */
    Image image;

    /**
     * the webpage url that we're extracting content from
     */
    String targetUrl;

    /**
     * stores a hash of our url for reference and image processing
     */
    String linkhash;

    /**
     * What's the minimum bytes for an image we'd accept is
     */
    int minBytesForImages;

    /**
     * location to store temporary image files if need be
     */
    String tempStoragePath;

    /**
     * holds the global configuration object
     */
    Configuration config;

    public BestImageGuesser(Configuration config, String targetUrl) {
        image = new Image();

        this.config = config;

        this.targetUrl = targetUrl;
        this.linkhash = md5(this.targetUrl);
    }

    public Image getBestImage(Document doc, Element topNode) {
        this.doc = doc;
        if (image.getImageSrc() == null) {
            this.checkForKnownElements();
        }

        // I'm checking for large images first because a lot of the meta tags contained thumbnail size images instead of the goods!
        // so we want to try and get the biggest image around the content area as possible.
        if (image.getImageSrc() == null) {
            this.checkForLargeImages(topNode, 0, 0);
        }

        // fall back to meta tags, these can sometimes be inconsistent which is why we favor them less
        if (image.getImageSrc() == null) {
            this.checkForMetaTag();
        }

        return image;
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

    private boolean checkForMetaTag() {
        return this.checkForLinkTag() || this.checkForOpenGraphTag();
    }

    /**
     * Checks to see if we were able to find open graph tags on this page
     */
    private boolean checkForOpenGraphTag() {
        try {
            Elements meta = this.doc.select("meta[property~=og:image]");
            // MM check this again, it's important
            // noinspection LoopStatementThatDoesntLoop
            for (int i = 0; i < meta.size(); i++) {
                Element item = meta.get(i);
                if (item.attr("content").length() < 1) {
                    return false;
                }
                String imagePath = this.buildImagePath(item.attr("content"));
                this.image.setImageSrc(imagePath);
                this.image.setImageExtractionType("opengraph");
                this.image.setConfidenceScore(100);
                this.image.setBytes(this.getBytesForImage(imagePath));
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks to see if we were able to find open graph tags on this page
     */
    private boolean checkForLinkTag() {
        try {
            Elements meta = this.doc.select("link[rel~=image_src]");
            // MM check this again, it's important
            // noinspection LoopStatementThatDoesntLoop
            for (int i = 0; i < meta.size(); i++) {
                Element item = meta.get(i);
                if (item.attr("href").length() < 1) {
                    return false;
                }
                this.image.setImageSrc(this.buildImagePath(item.attr("href")));
                this.image.setImageExtractionType("linktag");
                this.image.setConfidenceScore(100);
                this.image.setBytes(this.getBytesForImage(this.buildImagePath(item.attr("href"))));
                return true;
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    public ArrayList<Element> getAllImages() {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Although slow, this is the best way to determine the best image is to download them and check the actual dimensions of the image when
     * on disk so we'll go through a phased approach... 1. get a list of ALL images from the parent node 2. filter out any bad image names
     * that we know of (gifs, ads, etc..) 3. do a head request on each file to make sure it meets our bare requirements 4. any images left
     * over let's do a full GET request, download em to disk and check their dimensions 5. Score images based on different factors like
     * height/width and possibly things like color density
     */
    private void checkForLargeImages(Element node, int parentDepth, int siblingDepth) {
        if (node == null)
            return;

        Elements images = node.select("img");
        ArrayList<Element> goodImages = this.filterBadNames(images);
        goodImages = findImagesThatPassByteSizeTest(goodImages);

        HashMap<Element, Float> imageResults = downloadImagesAndGetResults(goodImages, parentDepth);

        // pick out image with high score
        Element highScoreImage = null;
        for (Element image : imageResults.keySet()) {
            if (highScoreImage == null) {
                highScoreImage = image;
            } else {

                if (imageResults.get(image) > imageResults.get(highScoreImage)) {
                    highScoreImage = image;
                }
            }
        }

        if (highScoreImage != null) {
            File f = new File(highScoreImage.attr("tempImagePath"));
            this.image.setTopImageNode(highScoreImage);
            this.image.setImageSrc(this.buildImagePath(highScoreImage.attr("src")));
            this.image.setImageExtractionType("bigimage");
            this.image.setBytes((int) f.length());
            if (imageResults.size() > 0) {
                this.image.setConfidenceScore(100 / imageResults.size());
            } else {
                this.image.setConfidenceScore(0);
            }
        } else {
            if (parentDepth < 2) {
                // we start at the top node then recursively go up to siblings/parent/grandparent to find something good
                Element prevSibling = node.previousElementSibling();
                if (prevSibling != null) {
                    siblingDepth++;
                    this.checkForLargeImages(prevSibling, parentDepth, siblingDepth);
                } else {
                    parentDepth++;
                    this.checkForLargeImages(node.parent(), parentDepth, siblingDepth);
                }
            }
        }

    }

    /**
     * Loop through all the images and find the ones that have the best bytes to even make them a candidate
     */
    private ArrayList<Element> findImagesThatPassByteSizeTest(ArrayList<Element> images) {
        int cnt = 0;
        ArrayList<Element> goodImages = new ArrayList<>();
        for (Element image : images) {
            if (cnt > 30) {
                return goodImages;
            }
            int bytes = this.getBytesForImage(image.attr("src"));
            // we don't want anything over 10 megs
            if ((bytes == 0 || bytes > this.minBytesForImages) && bytes < 10 * 1024 * 1024) {
                goodImages.add(image);
            } else {
                image.remove();
            }
            cnt++;
        }
        return goodImages;
    }

    /**
     * Takes a list of image elements and filters out the ones with bad names
     */
    private ArrayList<Element> filterBadNames(Elements images) {
        ArrayList<Element> goodImages = new ArrayList<>();
        for (Element image : images) {
            if (this.isOkImageFileName(image)) {
                goodImages.add(image);
            } else {
                image.remove();
            }
        }
        return goodImages;
    }

    /**
     * Will check the image src against a list of bad image files we know of like buttons, etc...
     */
    private boolean isOkImageFileName(Element imageNode) {
        String imgSrc = imageNode.attr("src");
        if (string.isNullOrEmpty(imgSrc)) {
            return false;
        }
        matchBadImageNames.reset(imgSrc);
        return !matchBadImageNames.find();
    }

    private static final String[] knownIds = {
            "yn-story-related-media", "cnn_strylccimg300cntr", "big_photo"
    };

    /**
     * In here we check for known image contains from sites we've checked out like yahoo, tech crunch, etc... that have known places to look
     * for good images
     */
    private void checkForKnownElements() {
        Element knownImage = null;
        for (String knownName : knownIds) {
            try {
                Element known = this.doc.getElementById(knownName);

                if (known == null) {
                    known = this.doc.getElementsByClass(knownName).first();
                }

                if (known != null) {
                    Element mainImage = known.getElementsByTag("img").first();
                    if (mainImage != null) {
                        knownImage = mainImage;
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }

        if (knownImage != null) {
            String knownImgSrc = knownImage.attr("src");
            this.image.setImageSrc(this.buildImagePath(knownImgSrc));
            this.image.setImageExtractionType("known");
            this.image.setConfidenceScore(90);
            this.image.setBytes(this.getBytesForImage(knownImgSrc));
        }

    }

    /**
     * This method will take an image path and build out the absolute path to that image using the initial url we crawled so we can find a
     * link to the image if they use relative urls like ../myimage.jpg
     */
    private String buildImagePath(String image) {
        URL pageURL;
        String newImage = image.replace(" ", "%20");
        try {
            pageURL = new URL(this.targetUrl);
            URL imageURL = new URL(pageURL, image);
            newImage = imageURL.toString();
        } catch (MalformedURLException ignored) {
        }
        return newImage;
    }

    /**
     * Does the HTTP HEAD request to get the image bytes for this images
     */
    private int getBytesForImage(String imageSrc) {
        int bytes = 0;
        try {
            String content = this.buildImagePath(imageSrc);
            content = content.replace(" ", "%20");
            bytes = this.minBytesForImages + 1;

            try {
                GooseDownloader.ContentInfo info = GooseDownloader.getContentInfo(content, true);
                int currentBytes = info.size;
                if (info.mimeType.contains("image")) {
                    bytes = currentBytes;
                }
            } catch (NullPointerException ignored) {
            }
        } catch (Exception ignored) {
        }

        return bytes;
    }

    /**
     * Download the images to temp disk and set their dimensions
     * <p/>
     * we're going to score the images in the order in which they appear so images higher up will have more importance, we'll count the area
     * of the 1st image as a score of 1 and then calculate how much larger or small each image after it is we'll also make sure to try and
     * weed out banner type ad blocks that have big widths and small heights or vice versa so if the image is 3rd found in the dom it's
     * sequence score would be 1 / 3 = .33 * diff in area from the first image
     */
    private HashMap<Element, Float> downloadImagesAndGetResults(ArrayList<Element> images, int depthLevel) {
        HashMap<Element, Float> imageResults = new HashMap<>();

        int cnt = 1;
        int initialArea = 0;

        for (Element image : images) {
            if (cnt > 30) {
                break;
            }

            // download image to local disk
            try {
                String imageSource = this.buildImagePath(image.attr("src"));

                String cachePath = ImageSaver.storeTempImage(linkhash, imageSource, config.getCacheDirectory(), config.getMinBytesForImages());
                if (cachePath == null) {
                    continue;
                }

                // set the temporary image path as an attribute on this node
                image.attr("tempImagePath", cachePath);

                ImageDetails imageDims = ImageUtils.getImageDetails(cachePath);
                int width = imageDims.getWidth();
                int height = imageDims.getHeight();

                // check for minimum depth requirements, if we're branching out wider in the dom, only get big images
                if (depthLevel > 1) {
                    if (width < 300) {
                        continue;
                    }
                }

                // Check dimensions to make sure it doesn't seem like a banner type ad
                if (this.isBannerDimensions(width, height)) {
                    image.remove();
                    continue;
                }

                if (width < 50) {
                    image.remove();
                    continue;
                }

                float sequenceScore = (float) 1 / cnt;
                int area = width * height;

                float totalScore;
                if (initialArea == 0) {
                    initialArea = area;
                    totalScore = 1;
                } else {
                    // let's see how many times larger this image is than the inital image
                    float areaDifference = (float) area / initialArea;
                    totalScore = sequenceScore * areaDifference;
                }

                cnt++;
                imageResults.put(image, totalScore);
            } catch (Exception ignored) {
            }
        }

        return imageResults;
    }

    /**
     * Returns true if we think this is kind of a bannery dimension like 600 / 100 = 6 may be a fishy dimension for a good image
     */

    private boolean isBannerDimensions(Integer width, Integer height) {
        if (width.equals(height)) {
            return false;
        }

        if (width > height) {
            float diff = (float) width / height;
            if (diff > 5) {
                return true;
            }
        }

        if (height > width) {
            float diff = (float) height / width;
            if (diff > 5) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public int getMinBytesForImages() {
        return minBytesForImages;
    }

    @SuppressWarnings("unused")
    public void setMinBytesForImages(int minBytesForImages) {
        this.minBytesForImages = minBytesForImages;
    }

    @SuppressWarnings("unused")
    public String getTempStoragePath() {
        return tempStoragePath;
    }

    @SuppressWarnings("unused")
    public void setTempStoragePath(String tempStoragePath) {
        this.tempStoragePath = tempStoragePath;
    }

}
