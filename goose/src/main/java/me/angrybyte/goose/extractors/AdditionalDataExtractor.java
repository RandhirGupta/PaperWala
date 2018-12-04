
package me.angrybyte.goose.extractors;

/**
 * Created by IntelliJ IDEA. User: robbie Date: 5/19/11 Time: 9:57 PM
 */

import org.jsoup.nodes.Element;

import java.util.Map;

import me.angrybyte.goose.Article;

/**
 * Implement this abstract class to extract anything not currently contained within the {@link Article} class
 */
public abstract class AdditionalDataExtractor implements Extractor<Map<String, String>> {

    public abstract Map<String, String> extract(Element rootElement);

}
