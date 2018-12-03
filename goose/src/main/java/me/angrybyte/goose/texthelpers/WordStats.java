/**
 * Licensed to Gravity.com under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Gravity.com licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package me.angrybyte.goose.texthelpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Counts various stuff related to words.
 */
public class WordStats {

    public static final WordStats EMPTY = new WordStats();

    /**
     * Total number of stop words or good words that we can calculate
     */
    private int stopWordCount = 0;

    /**
     * Total number of words on a node
     */
    private int wordCount = 0;

    /**
     * Holds an actual list of the stop words we found
     */
    private List<String> stopWords = new ArrayList<>();

    @SuppressWarnings("unused")
    public List<String> getStopWords() {
        return stopWords;
    }

    public void setStopWords(List<String> stopWords) {
        this.stopWords = stopWords;
    }

    public int getStopWordCount() {
        return stopWordCount;
    }

    public void setStopWordCount(int stopWordCount) {
        this.stopWordCount = stopWordCount;
    }

    @SuppressWarnings("unused")
    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

}
