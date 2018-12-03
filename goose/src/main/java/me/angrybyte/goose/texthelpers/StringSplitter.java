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
 * Helps to split strings.
 */
public class StringSplitter {

    private Pattern pattern;

    public StringSplitter(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public String[] split(String input) {
        if (string.isNullOrEmpty(input)) return string.emptyArray;
        return pattern.split(input);
    }
}


