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

package me.angrybyte.goose.texthelpers;

import java.util.regex.Pattern;

/**
 * Helps to replace strings within other strings.
 */
public class StringReplacement {

    private Pattern pattern;
    private String replaceWith;

    private StringReplacement(Pattern pattern, String replaceWith) {
        this.pattern = pattern;
        this.replaceWith = replaceWith;
    }

    public static StringReplacement compile(String pattern, String replaceWith) {
        if (string.isNullOrEmpty(pattern)) throw new IllegalArgumentException("Patterns must not be null or empty!");
        Pattern p = Pattern.compile(pattern);
        return new StringReplacement(p, replaceWith);
    }

    public String replaceAll(String input) {
        if (string.isNullOrEmpty(input)) return string.empty;
        return pattern.matcher(input).replaceAll(replaceWith);
    }
}


