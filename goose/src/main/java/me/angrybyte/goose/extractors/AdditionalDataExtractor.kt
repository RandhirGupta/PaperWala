package me.angrybyte.goose.extractors

/**
 * Created by IntelliJ IDEA. User: robbie Date: 5/19/11 Time: 9:57 PM
 */

import me.angrybyte.goose.Article
import org.jsoup.nodes.Element

/**
 * Implement this abstract class to extract anything not currently contained within the [Article] class
 */
abstract class AdditionalDataExtractor : Extractor<Map<String, String>> {

    abstract override fun extract(rootElement: Element): Map<String, String>

}
