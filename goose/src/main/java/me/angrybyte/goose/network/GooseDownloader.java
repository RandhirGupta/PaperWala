package me.angrybyte.goose.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * A rework of the old {@code HtmlFetcher} that works with Android platform. Helps you download HTML and Bitmaps.
 */
public class GooseDownloader {

    private static final String TAG = GooseDownloader.class.getSimpleName();
    private static final int MAX_BYTES = 15 * 1024 * 1024;

    private static final String AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4";
    private static final String CONTENT = "application/xml,application/xhtml+xml,text/html,application/javascript;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";

    /**
     * A helper class to store HTTP info before downloading the content.
     */
    public static final class ContentInfo {

        public final String url;
        public final String mimeType;
        public final int size;

        public static final ContentInfo EMPTY = new ContentInfo("", 0, "");

        public ContentInfo(String url, int size, String mimeType) {
            super();
            this.url = url;
            this.mimeType = mimeType;
            this.size = size;
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
     * @throws MaxBytesException Maximum page size must be smaller than {@link #MAX_BYTES}, or this will be thrown
     * @throws NotHtmlException  If we determine it's not HTML, this will be thrown
     */
    public static String getHtml(String textUrl, boolean followRedirects) throws IOException, MaxBytesException, NotHtmlException {
        HttpURLConnection connection = null;
        InputStream stream = null;

        try {
            connection = prepareConnection(textUrl, followRedirects);
            connection.connect();
            if (connection.getContentLength() > MAX_BYTES) {
                throw new MaxBytesException();
            }

            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response code for " + connection.getURL().toExternalForm() + " was " + response);
            }

            stream = connection.getInputStream();
            String content = convertStream(stream);

            if (TextUtils.isEmpty(content)) {
                throw new NotHtmlException();
            }

            String mimeType = connection.getContentType(); // MM make this more loose (don't check for type)?
            if (mimeType == null || (!mimeType.contains("text/html") && !mimeType.contains("text/xml") && !mimeType.contains("application/xml"))) {
                throw new NotHtmlException();
            }

            // ok it's non-empty HTML definitely
            return content;
        } finally {
            close(stream);
            disconnect(connection);
        }
    }

    /**
     * Prepares and opens a HTTP connection to the given URL. Sets all required request parameters and follows redirects.
     *
     * @param url Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return Either a {@link HttpURLConnection} if everything works fine, or throws an exception if something goes wrong
     *
     * @throws IOException Usually occurs when URL connection fails or you have an invalid URL
     */
    private static HttpURLConnection prepareConnection(String url, boolean followRedirects) throws IOException {
        URL base, next, parsed;
        HttpURLConnection conn;

        // loop for redirects
        while (true) {
            parsed = new URL(url);
            conn = (HttpURLConnection) parsed.openConnection();
            // we will handle redirects manually, because automatic redirect
            // works only when the same protocol is being used
            conn.setInstanceFollowRedirects(!followRedirects);
            conn.setReadTimeout(10 * 1000);
            conn.setConnectTimeout(15 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setUseCaches(true);

            conn.setRequestProperty("User-agent", AGENT);
            conn.setRequestProperty("http.User-Agent", AGENT);
            conn.setRequestProperty("http.protocol.cookie-policy", "compatibility");
            conn.setRequestProperty("http.language.Accept-Language", "en-us"); // MM do we need this?
            conn.setRequestProperty("http.protocol.content-charset", "UTF-8");
            conn.setRequestProperty("Accept", CONTENT);
            conn.setRequestProperty("Cache-Control", "max-age=0");
            conn.setRequestProperty("http.connection.stalecheck", "false"); // stale check impacts performance

            // MM this may not be needed either
            conn.setRequestProperty("http.conn-manager.timeout", "120000");
            conn.setRequestProperty("http.protocol.wait-for-continue", "10000");
            conn.setRequestProperty("http.tcp.nodelay", "true");

            if (followRedirects) {
                switch (conn.getResponseCode()) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        String location = conn.getHeaderField("Location");
                        // deal with relative URLs, don't reuse instance (bugs...)
                        base = new URL(url);
                        next = new URL(base, location);
                        url = next.toExternalForm();
                        continue;
                }
            }
            break;
        }

        return conn;
    }

    /**
     * Calls {@link HttpURLConnection#disconnect()} on the given connection object.
     *
     * @param connection Which connection to use, can be {@code null}
     */
    private static void disconnect(HttpURLConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Calls {@link Closeable#close()} on the given closeable object.
     *
     * @param closeable Which closeable to use, can be {@code null}
     */
    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Downloads a Bitmap image from the given URL. Follows redirects.
     *
     * @param textUrl Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return A Bitmap object if download succeeds, or {@code null} if download fails
     */
    public static Bitmap getPhoto(String textUrl, boolean followRedirects) {
        HttpURLConnection connection = null;
        InputStream stream = null;
        try {
            connection = prepareConnection(textUrl, followRedirects);
            connection.connect();
            stream = connection.getInputStream();
            return BitmapFactory.decodeStream(stream);
        } catch (Exception e) {
            Log.e(TAG, "Bitmap download exception");
            return null;
        } finally {
            close(stream);
            disconnect(connection);
        }
    }

    /**
     * Returns {@link ContentInfo} for the given URL.
     *
     * @param textUrl Which URL to use
     * @param followRedirects Whether to follow 301 and 302 server redirect codes
     * @return Either a content info object, or {@link ContentInfo#EMPTY} if something fails
     */
    public static ContentInfo getContentInfo(String textUrl, boolean followRedirects) {
        HttpURLConnection connection = null;
        try {
            connection = prepareConnection(textUrl, followRedirects);
            connection.connect();
            int size = connection.getContentLength();
            String type = connection.getContentType();
            return new ContentInfo(textUrl, size, type);
        } catch (Exception e) {
            Log.e(TAG, "Cannot get content info from " + textUrl, e);
            return ContentInfo.EMPTY;
        } finally {
            disconnect(connection);
        }
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream An {@link InputStream} to convert
     * @return Either the full text from the stream, or an empty string
     *
     * @throws IOException Usually occurs when URL connection fails or you have an invalid URL
     */
    public static String convertStream(InputStream stream) throws IOException {
        Scanner s = new Scanner(stream, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
