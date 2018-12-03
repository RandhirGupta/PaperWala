package me.angrybyte.goose.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

/**
 * A rework of the old `HtmlFetcher` that works with Android platform. Helps you download HTML and Bitmaps.
 */
object GooseDownloader {

    private val TAG = GooseDownloader::class.java.simpleName
    private val MAX_BYTES = 15 * 1024 * 1024

    private val AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4"
    private val CONTENT = "application/xml,application/xhtml+xml,text/html,application/javascript;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"

    /**
     * A helper class to store HTTP info before downloading the content.
     */
    class ContentInfo(val url: String, val size: Int, val mimeType: String) {
        companion object {

            val EMPTY = ContentInfo("", 0, "")
        }
    }

    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves the web page content as a InputStream, which it returns as a string. Follows redirects.
     *
     * @param textUrl Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return HTML content as text, or throws an exception if something goes wrong
     *
     * @throws IOException       Usually occurs when URL connection fails or you have an invalid URL
     * @throws MaxBytesException Maximum page size must be smaller than [.MAX_BYTES], or this will be thrown
     * @throws NotHtmlException  If we determine it's not HTML, this will be thrown
     */
    @Throws(IOException::class, MaxBytesException::class, NotHtmlException::class)
    fun getHtml(textUrl: String, followRedirects: Boolean): String {
        var connection: HttpURLConnection? = null
        var stream: InputStream? = null

        try {
            connection = prepareConnection(textUrl, followRedirects)
            connection.connect()
            if (connection.contentLength > MAX_BYTES) {
                throw MaxBytesException()
            }

            val response = connection.responseCode
            if (response != HttpURLConnection.HTTP_OK) {
                throw IOException("Response code for " + connection.url.toExternalForm() + " was " + response)
            }

            stream = connection.inputStream
            val content = convertStream(stream)

            if (TextUtils.isEmpty(content)) {
                throw NotHtmlException()
            }

            val mimeType = connection.contentType // MM make this more loose (don't check for type)?
            if (mimeType == null || !mimeType.contains("text/html") && !mimeType.contains("text/xml") && !mimeType.contains("application/xml")) {
                throw NotHtmlException()
            }

            // ok it's non-empty HTML definitely
            return content
        } finally {
            close(stream)
            disconnect(connection)
        }
    }

    /**
     * Prepares and opens a HTTP connection to the given URL. Sets all required request parameters and follows redirects.
     *
     * @param url Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return Either a [HttpURLConnection] if everything works fine, or throws an exception if something goes wrong
     *
     * @throws IOException Usually occurs when URL connection fails or you have an invalid URL
     */
    @Throws(IOException::class)
    private fun prepareConnection(url: String, followRedirects: Boolean): HttpURLConnection {
        var url = url
        var base: URL
        var next: URL
        var parsed: URL
        var conn: HttpURLConnection

        // loop for redirects
        loop@ while (true) {
            parsed = URL(url)
            conn = parsed.openConnection() as HttpURLConnection
            // we will handle redirects manually, because automatic redirect
            // works only when the same protocol is being used
            conn.instanceFollowRedirects = !followRedirects
            conn.readTimeout = 10 * 1000
            conn.connectTimeout = 15 * 1000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.useCaches = true

            conn.setRequestProperty("User-agent", AGENT)
            conn.setRequestProperty("http.User-Agent", AGENT)
            conn.setRequestProperty("http.protocol.cookie-policy", "compatibility")
            conn.setRequestProperty("http.language.Accept-Language", "en-us") // MM do we need this?
            conn.setRequestProperty("http.protocol.content-charset", "UTF-8")
            conn.setRequestProperty("Accept", CONTENT)
            conn.setRequestProperty("Cache-Control", "max-age=0")
            conn.setRequestProperty("http.connection.stalecheck", "false") // stale check impacts performance

            // MM this may not be needed either
            conn.setRequestProperty("http.conn-manager.timeout", "120000")
            conn.setRequestProperty("http.protocol.wait-for-continue", "10000")
            conn.setRequestProperty("http.tcp.nodelay", "true")

            if (followRedirects) {
                when (conn.responseCode) {
                    HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP -> {
                        val location = conn.getHeaderField("Location")
                        // deal with relative URLs, don't reuse instance (bugs...)
                        base = URL(url)
                        next = URL(base, location)
                        url = next.toExternalForm()
                        continue@loop
                    }
                }
            }
            break
        }

        return conn
    }

    /**
     * Calls [HttpURLConnection.disconnect] on the given connection object.
     *
     * @param connection Which connection to use, can be `null`
     */
    private fun disconnect(connection: HttpURLConnection?) {
        if (connection != null) {
            try {
                connection.disconnect()
            } catch (ignored: Exception) {
            }

        }
    }

    /**
     * Calls [Closeable.close] on the given closeable object.
     *
     * @param closeable Which closeable to use, can be `null`
     */
    private fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (ignored: Exception) {
            }

        }
    }

    /**
     * Downloads a Bitmap image from the given URL. Follows redirects.
     *
     * @param textUrl Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return A Bitmap object if download succeeds, or `null` if download fails
     */
    fun getPhoto(textUrl: String, followRedirects: Boolean): Bitmap? {
        var connection: HttpURLConnection? = null
        var stream: InputStream? = null
        try {
            connection = prepareConnection(textUrl, followRedirects)
            connection.connect()
            stream = connection.inputStream
            return BitmapFactory.decodeStream(stream)
        } catch (e: Exception) {
            Log.e(TAG, "Bitmap download exception")
            return null
        } finally {
            close(stream)
            disconnect(connection)
        }
    }

    /**
     * Returns [ContentInfo] for the given URL.
     *
     * @param textUrl Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return Either a content info object, or [ContentInfo.EMPTY] if something fails
     */
    fun getContentInfo(textUrl: String, followRedirects: Boolean): ContentInfo {
        var connection: HttpURLConnection? = null
        try {
            connection = prepareConnection(textUrl, followRedirects)
            connection.connect()
            val size = connection.contentLength
            val type = connection.contentType
            return ContentInfo(textUrl, size, type)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot get content info from $textUrl", e)
            return ContentInfo.EMPTY
        } finally {
            disconnect(connection)
        }
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream An [InputStream] to convert
     * @return Either the full text from the stream, or an empty string
     *
     * @throws IOException Usually occurs when URL connection fails or you have an invalid URL
     */
    @Throws(IOException::class)
    fun convertStream(stream: InputStream?): String {
        val s = Scanner(stream!!, "UTF-8").useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}
