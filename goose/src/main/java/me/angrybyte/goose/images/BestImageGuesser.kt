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

package me.angrybyte.goose.images

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

import me.angrybyte.goose.Configuration
import me.angrybyte.goose.network.GooseDownloader
import me.angrybyte.goose.texthelpers.string

/**
 * This image extractor will attempt to find the best image nearest the article. Unfortunately this is a slow process since we're actually
 * downloading the image itself to inspect it's actual height/width and area metrics since most of the time these aren't in the image tags
 * themselves or can be falsified. We'll weight the images in descending order depending on how high up they are compared to the top node
 * content
 */
class BestImageGuesser(
        /**
         * holds the global configuration object
         */
        internal var config: Configuration,
        /**
         * the webpage url that we're extracting content from
         */
        internal var targetUrl: String) : ImageExtractor {

    /**
     * holds the document that we're extracting the image from
     */
    internal lateinit var doc: Document

    /**
     * holds the result of our image extraction
     */
    internal var image: Image

    /**
     * stores a hash of our url for reference and image processing
     */
    internal var linkhash: String

    /**
     * What's the minimum bytes for an image we'd accept is
     */
    var minBytesForImages: Int = 0

    /**
     * location to store temporary image files if need be
     */
    lateinit var tempStoragePath: String

    init {
        image = Image()
        this.linkhash = md5(this.targetUrl)
    }

    override fun getBestImage(doc: Document, topNode: Element): Image {
        this.doc = doc
        if (image.imageSrc == null) {
            this.checkForKnownElements()
        }

        // I'm checking for large images first because a lot of the meta tags contained thumbnail size images instead of the goods!
        // so we want to try and get the biggest image around the content area as possible.
        if (image.imageSrc == null) {
            this.checkForLargeImages(topNode, 0, 0)
        }

        // fall back to meta tags, these can sometimes be inconsistent which is why we favor them less
        if (image.imageSrc == null) {
            this.checkForMetaTag()
        }

        return image
    }

    private fun checkForMetaTag(): Boolean {
        return this.checkForLinkTag() || this.checkForOpenGraphTag()
    }

    /**
     * Checks to see if we were able to find open graph tags on this page
     */
    private fun checkForOpenGraphTag(): Boolean {
        try {
            val meta = this.doc.select("meta[property~=og:image]")
            // MM check this again, it's important

            for (i in meta.indices) {
                val item = meta[i]
                if (item.attr("content").length < 1) {
                    return false
                }
                val imagePath = this.buildImagePath(item.attr("content"))
                this.image.imageSrc = imagePath
                this.image.imageExtractionType = "opengraph"
                this.image.confidenceScore = 100.0
                this.image.bytes = this.getBytesForImage(imagePath)
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * Checks to see if we were able to find open graph tags on this page
     */
    private fun checkForLinkTag(): Boolean {
        try {
            val meta = this.doc.select("link[rel~=image_src]")
            // MM check this again, it's important

            for (i in meta.indices) {
                val item = meta[i]
                if (item.attr("href").length < 1) {
                    return false
                }
                this.image.imageSrc = this.buildImagePath(item.attr("href"))
                this.image.imageExtractionType = "linktag"
                this.image.confidenceScore = 100.0
                this.image.bytes = this.getBytesForImage(this.buildImagePath(item.attr("href")))
                return true
            }
            return false
        } catch (ignored: Exception) {
            return false
        }

    }

    override fun getAllImages(): ArrayList<Element>? {
        return null //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Although slow, this is the best way to determine the best image is to download them and check the actual dimensions of the image when
     * on disk so we'll go through a phased approach... 1. get a list of ALL images from the parent node 2. filter out any bad image names
     * that we know of (gifs, ads, etc..) 3. do a head request on each file to make sure it meets our bare requirements 4. any images left
     * over let's do a full GET request, download em to disk and check their dimensions 5. Score images based on different factors like
     * height/width and possibly things like color density
     */
    private fun checkForLargeImages(node: Element?, parentDepth: Int, siblingDepth: Int) {
        var parentDepth = parentDepth
        var siblingDepth = siblingDepth
        if (node == null)
            return

        val images = node.select("img")
        var goodImages = this.filterBadNames(images)
        goodImages = findImagesThatPassByteSizeTest(goodImages)

        val imageResults = downloadImagesAndGetResults(goodImages, parentDepth)

        // pick out image with high score
        var highScoreImage: Element? = null
        for (image in imageResults.keys) {
            if (highScoreImage == null) {
                highScoreImage = image
            } else {

                if (imageResults[image] > imageResults[highScoreImage]) {
                    highScoreImage = image
                }
            }
        }

        if (highScoreImage != null) {
            val f = File(highScoreImage.attr("tempImagePath"))
            this.image.topImageNode = highScoreImage
            this.image.imageSrc = this.buildImagePath(highScoreImage.attr("src"))
            this.image.imageExtractionType = "bigimage"
            this.image.bytes = f.length().toInt()
            if (imageResults.size > 0) {
                this.image.confidenceScore = (100 / imageResults.size).toDouble()
            } else {
                this.image.confidenceScore = 0.0
            }
        } else {
            if (parentDepth < 2) {
                // we start at the top node then recursively go up to siblings/parent/grandparent to find something good
                val prevSibling = node.previousElementSibling()
                if (prevSibling != null) {
                    siblingDepth++
                    this.checkForLargeImages(prevSibling, parentDepth, siblingDepth)
                } else {
                    parentDepth++
                    this.checkForLargeImages(node.parent(), parentDepth, siblingDepth)
                }
            }
        }

    }

    /**
     * Loop through all the images and find the ones that have the best bytes to even make them a candidate
     */
    private fun findImagesThatPassByteSizeTest(images: ArrayList<Element>): ArrayList<Element> {
        var cnt = 0
        val goodImages = ArrayList<Element>()
        for (image in images) {
            if (cnt > 30) {
                return goodImages
            }
            val bytes = this.getBytesForImage(image.attr("src"))
            // we don't want anything over 10 megs
            if ((bytes == 0 || bytes > this.minBytesForImages) && bytes < 10 * 1024 * 1024) {
                goodImages.add(image)
            } else {
                image.remove()
            }
            cnt++
        }
        return goodImages
    }

    /**
     * Takes a list of image elements and filters out the ones with bad names
     */
    private fun filterBadNames(images: Elements): ArrayList<Element> {
        val goodImages = ArrayList<Element>()
        for (image in images) {
            if (this.isOkImageFileName(image)) {
                goodImages.add(image)
            } else {
                image.remove()
            }
        }
        return goodImages
    }

    /**
     * Will check the image src against a list of bad image files we know of like buttons, etc...
     */
    private fun isOkImageFileName(imageNode: Element): Boolean {
        val imgSrc = imageNode.attr("src")
        if (string.isNullOrEmpty(imgSrc)) {
            return false
        }
        matchBadImageNames.reset(imgSrc)
        return !matchBadImageNames.find()
    }

    /**
     * In here we check for known image contains from sites we've checked out like yahoo, tech crunch, etc... that have known places to look
     * for good images
     */
    private fun checkForKnownElements() {
        var knownImage: Element? = null
        for (knownName in knownIds) {
            try {
                var known: Element? = this.doc.getElementById(knownName)

                if (known == null) {
                    known = this.doc.getElementsByClass(knownName).first()
                }

                if (known != null) {
                    val mainImage = known.getElementsByTag("img").first()
                    if (mainImage != null) {
                        knownImage = mainImage
                    }
                }
            } catch (ignored: NullPointerException) {
            }

        }

        if (knownImage != null) {
            val knownImgSrc = knownImage.attr("src")
            this.image.imageSrc = this.buildImagePath(knownImgSrc)
            this.image.imageExtractionType = "known"
            this.image.confidenceScore = 90.0
            this.image.bytes = this.getBytesForImage(knownImgSrc)
        }

    }

    /**
     * This method will take an image path and build out the absolute path to that image using the initial url we crawled so we can find a
     * link to the image if they use relative urls like ../myimage.jpg
     */
    private fun buildImagePath(image: String): String {
        val pageURL: URL
        var newImage = image.replace(" ", "%20")
        try {
            pageURL = URL(this.targetUrl)
            val imageURL = URL(pageURL, image)
            newImage = imageURL.toString()
        } catch (ignored: MalformedURLException) {
        }

        return newImage
    }

    /**
     * Does the HTTP HEAD request to get the image bytes for this images
     */
    private fun getBytesForImage(imageSrc: String): Int {
        var bytes = 0
        try {
            var content = this.buildImagePath(imageSrc)
            content = content.replace(" ", "%20")
            bytes = this.minBytesForImages + 1

            try {
                val info = GooseDownloader.getContentInfo(content, true)
                val currentBytes = info.size
                if (info.mimeType.contains("image")) {
                    bytes = currentBytes
                }
            } catch (ignored: NullPointerException) {
            }

        } catch (ignored: Exception) {
        }

        return bytes
    }

    /**
     * Download the images to temp disk and set their dimensions
     *
     *
     * we're going to score the images in the order in which they appear so images higher up will have more importance, we'll count the area
     * of the 1st image as a score of 1 and then calculate how much larger or small each image after it is we'll also make sure to try and
     * weed out banner type ad blocks that have big widths and small heights or vice versa so if the image is 3rd found in the dom it's
     * sequence score would be 1 / 3 = .33 * diff in area from the first image
     */
    private fun downloadImagesAndGetResults(images: ArrayList<Element>, depthLevel: Int): HashMap<Element, Float> {
        val imageResults = HashMap<Element, Float>()

        var cnt = 1
        var initialArea = 0

        for (image in images) {
            if (cnt > 30) {
                break
            }

            // download image to local disk
            try {
                val imageSource = this.buildImagePath(image.attr("src"))

                val cachePath = ImageSaver.storeTempImage(linkhash, imageSource, config.cacheDirectory, config.minBytesForImages)
                        ?: continue

                // set the temporary image path as an attribute on this node
                image.attr("tempImagePath", cachePath)

                val imageDims = ImageUtils.getImageDetails(cachePath)
                val width = imageDims.width
                val height = imageDims.height

                // check for minimum depth requirements, if we're branching out wider in the dom, only get big images
                if (depthLevel > 1) {
                    if (width < 300) {
                        continue
                    }
                }

                // Check dimensions to make sure it doesn't seem like a banner type ad
                if (this.isBannerDimensions(width, height)) {
                    image.remove()
                    continue
                }

                if (width < 50) {
                    image.remove()
                    continue
                }

                val sequenceScore = 1.toFloat() / cnt
                val area = width * height

                val totalScore: Float
                if (initialArea == 0) {
                    initialArea = area
                    totalScore = 1f
                } else {
                    // let's see how many times larger this image is than the inital image
                    val areaDifference = area.toFloat() / initialArea
                    totalScore = sequenceScore * areaDifference
                }

                cnt++
                imageResults[image] = totalScore
            } catch (ignored: Exception) {
            }

        }

        return imageResults
    }

    /**
     * Returns true if we think this is kind of a bannery dimension like 600 / 100 = 6 may be a fishy dimension for a good image
     */

    private fun isBannerDimensions(width: Int, height: Int?): Boolean {
        if (width == height) {
            return false
        }

        if (width > height!!) {
            val diff = width as Float / height!!
            if (diff > 5) {
                return true
            }
        }

        if (height > width) {
            val diff = height as Float / width
            if (diff > 5) {
                return true
            }
        }
        return false
    }

    companion object {

        /**
         * This lists all the known bad button names that we have
         */
        private val matchBadImageNames: Matcher

        init {
            val negatives = (".html|.gif|.ico|button|twitter.jpg|facebook.jpg|digg.jpg|digg.png|delicious.png|facebook.png|reddit" + ""
                    + ".jpg|doubleclick|diggthis|diggThis|adserver|/ads/|ec.atdmt.com") + "|mediaplex.com|adsatt|view.atdmt"
            // create negative elements
            matchBadImageNames = Pattern.compile(negatives).matcher(string.empty)
        }

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

        private val knownIds = arrayOf("yn-story-related-media", "cnn_strylccimg300cntr", "big_photo")
    }

}
