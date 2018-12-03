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

package me.angrybyte.goose.texthelpers

/**
 * Created by IntelliJ IDEA. User: robbie Date: 5/13/11 Time: 12:03 AM
 */

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Wraps the usage of making multiple string replacements in an ordered sequence. For Example... instead of doing this over and over:
 * <blockquote>
 *
 * <pre>
 * String text = "   Some example text     ";
 * text = text.[replaceAll][String.replaceAll]("e", "E");
 * text = text.[replaceAll][String.replaceAll](" ", "_");
 * text = text.[replaceAll][String.replaceAll]("^\\s+$", "");
</pre> *
 *
</blockquote> *  You can use a `ReplaceSequence` like this: <blockquote>
 *
 * <pre>
 * static final betterReplacements = ReplaceSequence
 * .[create][.create]("e", "E").[append][.append](" ", "_").[append][.append]("^\\s+$");
 *
 * void fixMyString(String text) {
 * return betterReplacements.[replaceAll][.replaceAll](text);
 * }
</pre> *
 *
</blockquote> *
 *
 *
 * Internally, an ordered list of [Matcher]s and its associated replacement is built as the [.append] method is called.<br></br>
 * Each matcher is [reset][Matcher.reset] with the input specified in the [.replaceAll] method.
 *
 * Use of this class can improve performance if the sequence of replacements is intended to be used repeatedly throughout the life of an
 * application.<br></br>
 * This is due to the fact that each [Pattern] is only compiled once and each [Matcher] is only generated once.
 */
class ReplaceSequence private constructor(pair: StringReplacement) {

    // shhhhh... it's private!
    private val replacements = ArrayList<StringReplacement>()

    /**
     * Appends a new pattern to this instance in a builder pattern
     *
     * @param pattern The regex [pattern][Pattern] [String] for this replacement
     * @param replaceWith The [String] to replace matches of the specified pattern
     * @return this instance of itself for use in a builder pattern
     */
    @JvmOverloads
    fun append(pattern: String, replaceWith: String = string.empty): ReplaceSequence {
        replacements.add(StringReplacement.compile(pattern, replaceWith))
        return this
    }

    /**
     * Applies each of the replacements specified via the initial [.create] and/or any additional via [.append]
     *
     * @param input the [String] to apply all of the replacements to
     * @return the resulting [String] after all replacements have been applied
     */
    fun replaceAll(input: String): String {
        var input = input
        if (string.isNullOrEmpty(input))
            return string.empty
        for (rp in replacements) {
            input = rp.replaceAll(input)
        }

        return input
    }

    init {
        replacements.add(pair)
    }

    companion object {

        /**
         * Creates a new `ReplaceSequence` with the first pattern to be replaced with the specified `replaceWith`
         * parameter.
         *
         * @param firstPattern The regex [pattern][Pattern] [String] for the first replacement
         * @param replaceWith The [String] to replace matches of the specified pattern
         * @return a new instance
         */
        @JvmOverloads
        fun create(firstPattern: String, replaceWith: String = string.empty): ReplaceSequence {
            return ReplaceSequence(StringReplacement.compile(firstPattern, replaceWith))
        }
    }

}
/**
 * Creates a new `ReplaceSequence` with the first pattern to be replaced with an empty `String`
 *
 * @param firstPattern The regex [pattern][Pattern] string for the first replacement
 * @return a new instance
 */
/**
 * Appends a new pattern to this instance in a builder pattern
 *
 * @param pattern The regex [pattern][Pattern] [String] for this replacement
 * @return this instance of itself for use in a builder pattern
 */
