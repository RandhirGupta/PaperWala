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

import java.util.regex.Pattern

/**
 * Helps to replace strings within other strings.
 */
class StringReplacement private constructor(private val pattern: Pattern, private val replaceWith: String) {

    fun replaceAll(input: String): String {
        return if (string.isNullOrEmpty(input)) string.empty else pattern.matcher(input).replaceAll(replaceWith)
    }

    companion object {

        fun compile(pattern: String, replaceWith: String): StringReplacement {
            if (string.isNullOrEmpty(pattern)) throw IllegalArgumentException("Patterns must not be null or empty!")
            val p = Pattern.compile(pattern)
            return StringReplacement(p, replaceWith)
        }
    }
}


