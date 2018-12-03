/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.angrybyte.goose.outputformatters

import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.HashMap
import java.util.TreeMap

/**
 *
 *
 * Provides HTML and XML entity utilities.
 *
 *
 * @author [Alexander Day Chaffee](mailto:alex@purpletech.com)
 * @author [Gary Gregory](mailto:ggregory@seagullsw.com)
 * @version $Id: Entities.java 636641 2008-03-13 06:11:30Z bayard $
 * @see [ISO Entities](http://hotwired.lycos.com/webmonkey/reference/special_characters/)
 *
 * @see [HTML 3.2 Character Entities for ISO Latin-1](http://www.w3.org/TR/REC-html32.latin1)
 *
 * @see [HTML 4.0 Character entity references](http://www.w3.org/TR/REC-html40/sgml/entities.html)
 *
 * @see [HTML 4.01 Character References](http://www.w3.org/TR/html401/charset.html.h-5.3)
 *
 * @see [HTML 4.01 Code positions](http://www.w3.org/TR/html401/charset.html.code-position)
 *
 * @since 2.0
 */
class Entities {

    // package scoped for testing
    internal var map: EntityMap = Entities.LookupEntityMap()

    internal interface EntityMap {

        /**
         *
         *
         * Add an entry to this entity map.
         *
         *
         * @param name the entity name
         * @param value the entity value
         */
        fun add(name: String, value: Int)

        /**
         *
         *
         * Returns the name of the entity identified by the specified value.
         *
         *
         * @param value the value to locate
         * @return entity name associated with the specified value
         */
        fun name(value: Int): String

        /**
         *
         *
         * Returns the value of the entity identified by the specified name.
         *
         *
         * @param name the name to locate
         * @return entity value associated with the specified name
         */
        fun value(name: String): Int
    }

    internal open class PrimitiveEntityMap : EntityMap {

        private val mapNameToValue = HashMap()

        private val mapValueToName = IntHashMap()

        /**
         * {@inheritDoc}
         */
        override fun add(name: String, value: Int) {
            mapNameToValue.put(name, value)
            mapValueToName.put(value, name)
        }

        /**
         * {@inheritDoc}
         */
        override fun name(value: Int): String {
            return mapValueToName.get(value) as String
        }

        /**
         * {@inheritDoc}
         */
        override fun value(name: String): Int {
            val value = mapNameToValue.get(name) ?: return -1
            return (value as Int).toInt()
        }
    }

    internal abstract class MapIntMap : Entities.EntityMap {

        protected var mapNameToValue: MutableMap<*, *>? = null

        protected var mapValueToName: MutableMap<*, *>? = null

        /**
         * {@inheritDoc}
         */
        override fun add(name: String, value: Int) {
            mapNameToValue!![name] = value
            mapValueToName!![value] = name
        }

        /**
         * {@inheritDoc}
         */
        override fun name(value: Int): String {
            return mapValueToName!![value] as String
        }

        /**
         * {@inheritDoc}
         */
        override fun value(name: String): Int {
            val value = mapNameToValue!![name] ?: return -1
            return (value as Int).toInt()
        }
    }

    internal class HashEntityMap : MapIntMap() {
        /**
         * Constructs a new instance of `HashEntityMap`.
         */
        init {
            mapNameToValue = HashMap()
            mapValueToName = HashMap()
        }
    }

    internal class TreeEntityMap : MapIntMap() {
        /**
         * Constructs a new instance of `TreeEntityMap`.
         */
        init {
            mapNameToValue = TreeMap()
            mapValueToName = TreeMap()
        }
    }

    internal class LookupEntityMap : PrimitiveEntityMap() {

        private var lookupTable: Array<String>? = null

        private val LOOKUP_TABLE_SIZE = 256

        /**
         * {@inheritDoc}
         */
        override fun name(value: Int): String {
            return if (value < LOOKUP_TABLE_SIZE) {
                lookupTable()!![value]
            } else super.name(value)
        }

        /**
         *
         *
         * Returns the lookup table for this entity map. The lookup table is created if it has not been previously.
         *
         *
         * @return the lookup table
         */
        private fun lookupTable(): Array<String>? {
            if (lookupTable == null) {
                createLookupTable()
            }
            return lookupTable
        }

        /**
         *
         *
         * Creates an entity lookup table of LOOKUP_TABLE_SIZE elements, initialized with entity names.
         *
         */
        private fun createLookupTable() {
            lookupTable = arrayOfNulls(LOOKUP_TABLE_SIZE)
            for (i in 0 until LOOKUP_TABLE_SIZE) {
                lookupTable[i] = super.name(i)
            }
        }
    }

    internal open class ArrayEntityMap : EntityMap {

        protected var growBy = 100

        protected var size = 0

        protected var names: Array<String>

        protected var values: IntArray

        /**
         * Constructs a new instance of `ArrayEntityMap`.
         */
        constructor() {
            names = arrayOfNulls(growBy)
            values = IntArray(growBy)
        }

        /**
         * Constructs a new instance of `ArrayEntityMap` specifying the size by which the array should grow.
         *
         * @param growBy array will be initialized to and will grow by this amount
         */
        constructor(growBy: Int) {
            this.growBy = growBy
            names = arrayOfNulls(growBy)
            values = IntArray(growBy)
        }

        /**
         * {@inheritDoc}
         */
        override fun add(name: String, value: Int) {
            ensureCapacity(size + 1)
            names[size] = name
            values[size] = value
            size++
        }

        /**
         * Verifies the capacity of the entity array, adjusting the size if necessary.
         *
         * @param capacity size the array should be
         */
        protected fun ensureCapacity(capacity: Int) {
            if (capacity > names.size) {
                val newSize = Math.max(capacity, size + growBy)
                val newNames = arrayOfNulls<String>(newSize)
                System.arraycopy(names, 0, newNames, 0, size)
                names = newNames
                val newValues = IntArray(newSize)
                System.arraycopy(values, 0, newValues, 0, size)
                values = newValues
            }
        }

        /**
         * {@inheritDoc}
         */
        override fun name(value: Int): String? {
            for (i in 0 until size) {
                if (values[i] == value) {
                    return names[i]
                }
            }
            return null
        }

        /**
         * {@inheritDoc}
         */
        override fun value(name: String): Int {
            for (i in 0 until size) {
                if (names[i] == name) {
                    return values[i]
                }
            }
            return -1
        }
    }

    internal class BinaryEntityMap : ArrayEntityMap {

        /**
         * Constructs a new instance of `BinaryEntityMap`.
         */
        constructor() : super() {}

        /**
         * Constructs a new instance of `ArrayEntityMap` specifying the size by which the underlying array should grow.
         *
         * @param growBy array will be initialized to and will grow by this amount
         */
        constructor(growBy: Int) : super(growBy) {}

        /**
         * Performs a binary search of the entity array for the specified key. This method is based on code in [java.util.Arrays].
         *
         * @param key the key to be found
         * @return the index of the entity array matching the specified key
         */
        private fun binarySearch(key: Int): Int {
            var low = 0
            var high = size - 1

            while (low <= high) {
                val mid = (low + high).ushr(1)
                val midVal = values[mid]

                if (midVal < key) {
                    low = mid + 1
                } else if (midVal > key) {
                    high = mid - 1
                } else {
                    return mid // key found
                }
            }
            return -(low + 1) // key not found.
        }

        /**
         * {@inheritDoc}
         */
        override fun add(name: String, value: Int) {
            ensureCapacity(size + 1)
            var insertAt = binarySearch(value)
            if (insertAt > 0) {
                return  // note: this means you can't insert the same value twice
            }
            insertAt = -(insertAt + 1) // binarySearch returns it negative and off-by-one
            System.arraycopy(values, insertAt, values, insertAt + 1, size - insertAt)
            values[insertAt] = value
            System.arraycopy(names, insertAt, names, insertAt + 1, size - insertAt)
            names[insertAt] = name
            size++
        }

        /**
         * {@inheritDoc}
         */
        override fun name(value: Int): String? {
            val index = binarySearch(value)
            return if (index < 0) {
                null
            } else names[index]
        }
    }

    /**
     *
     *
     * Adds entities to this entity.
     *
     *
     * @param entityArray array of entities to be added
     */
    fun addEntities(entityArray: Array<Array<String>>) {
        for (i in entityArray.indices) {
            addEntity(entityArray[i][0], Integer.parseInt(entityArray[i][1]))
        }
    }

    /**
     *
     *
     * Add an entity to this entity.
     *
     *
     * @param name name of the entity
     * @param value vale of the entity
     */
    fun addEntity(name: String, value: Int) {
        map.add(name, value)
    }

    /**
     *
     *
     * Returns the name of the entity identified by the specified value.
     *
     *
     * @param value the value to locate
     * @return entity name associated with the specified value
     */
    fun entityName(value: Int): String? {
        return map.name(value)
    }

    /**
     *
     *
     * Returns the value of the entity identified by the specified name.
     *
     *
     * @param name the name to locate
     * @return entity value associated with the specified name
     */
    fun entityValue(name: String): Int {
        return map.value(name)
    }

    /**
     *
     *
     * Escapes the characters in a `String`.
     *
     *
     *
     *
     *
     * For example, if you have called addEntity(&quot;foo&quot;, 0xA1), escape(&quot;\u00A1&quot;) will return &quot;&amp;foo;&quot;
     *
     *
     * @param str The `String` to escape.
     * @return A new escaped `String`.
     */
    fun escape(str: String): String {
        val stringWriter = createStringWriter(str)
        try {
            this.escape(stringWriter, str)
        } catch (e: IOException) {
            // This should never happen because ALL the StringWriter methods called by #escape(Writer, String) do not
            // throw IOExceptions.
            throw RuntimeException(e)
        }

        return stringWriter.toString()
    }

    /**
     *
     *
     * Escapes the characters in the `String` passed and writes the result to the `Writer` passed.
     *
     *
     * @param writer The `Writer` to write the results of the escaping to. Assumed to be a non-null value.
     * @param str The `String` to escape. Assumed to be a non-null value.
     * @throws IOException when `Writer` passed throws the exception from calls to the [Writer.write] methods.
     * @see .escape
     * @see Writer
     */
    @Throws(IOException::class)
    fun escape(writer: Writer, str: String) {
        val len = str.length
        for (i in 0 until len) {
            val c = str[i]
            val entityName = this.entityName(c.toInt())
            if (entityName == null) {
                if (c.toInt() > 0x7F) {
                    writer.write("&#")
                    writer.write(Integer.toString(c.toInt(), 10))
                    writer.write(';')
                } else {
                    writer.write(c.toInt())
                }
            } else {
                writer.write('&')
                writer.write(entityName)
                writer.write(';')
            }
        }
    }

    /**
     *
     *
     * Unescapes the entities in a `String`.
     *
     *
     *
     *
     *
     * For example, if you have called addEntity(&quot;foo&quot;, 0xA1), unescape(&quot;&amp;foo;&quot;) will return &quot;\u00A1&quot;
     *
     *
     * @param str The `String` to escape.
     * @return A new escaped `String`.
     */
    fun unescape(str: String): String {
        val firstAmp = str.indexOf('&')
        if (firstAmp < 0) {
            return str
        } else {
            val stringWriter = createStringWriter(str)
            try {
                this.doUnescape(stringWriter, str, firstAmp)
            } catch (e: IOException) {
                // This should never happen because ALL the StringWriter methods called by #escape(Writer, String)
                // do not throw IOExceptions.
                throw RuntimeException(e)
            }

            return stringWriter.toString()
        }
    }

    /**
     * Make the StringWriter 10% larger than the source String to avoid growing the writer
     *
     * @param str The source string
     * @return A newly created StringWriter
     */
    private fun createStringWriter(str: String): StringWriter {
        return StringWriter((str.length + str.length * 0.1).toInt())
    }

    /**
     *
     *
     * Unescapes the escaped entities in the `String` passed and writes the result to the `Writer` passed.
     *
     *
     * @param writer The `Writer` to write the results to; assumed to be non-null.
     * @param str The source `String` to unescape; assumed to be non-null.
     * @throws IOException when `Writer` passed throws the exception from calls to the [Writer.write] methods.
     * @see .escape
     * @see Writer
     */
    @Throws(IOException::class)
    fun unescape(writer: Writer, str: String) {
        val firstAmp = str.indexOf('&')
        if (firstAmp < 0) {
            writer.write(str)
            return
        } else {
            doUnescape(writer, str, firstAmp)
        }
    }

    /**
     * Underlying unescape method that allows the optimisation of not starting from the 0 index again.
     *
     * @param writer The `Writer` to write the results to; assumed to be non-null.
     * @param str The source `String` to unescape; assumed to be non-null.
     * @param firstAmp The `int` index of the first ampersand in the source String.
     * @throws IOException when `Writer` passed throws the exception from calls to the [Writer.write] methods.
     */
    @Throws(IOException::class)
    private fun doUnescape(writer: Writer, str: String, firstAmp: Int) {
        writer.write(str, 0, firstAmp)
        val len = str.length
        var i = firstAmp
        while (i < len) {
            val c = str[i]
            if (c == '&') {
                val nextIdx = i + 1
                val semiColonIdx = str.indexOf(';', nextIdx)
                if (semiColonIdx == -1) {
                    writer.write(c.toInt())
                    i++
                    continue
                }
                val amphersandIdx = str.indexOf('&', i + 1)
                if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
                    // Then the text looks like &...&...;
                    writer.write(c.toInt())
                    i++
                    continue
                }
                val entityContent = str.substring(nextIdx, semiColonIdx)
                var entityValue = -1
                val entityContentLen = entityContent.length
                if (entityContentLen > 0) {
                    if (entityContent[0] == '#') { // escaped value content is an integer (decimal or
                        // hexidecimal)
                        if (entityContentLen > 1) {
                            val isHexChar = entityContent[1]
                            try {
                                when (isHexChar) {
                                    'X', 'x' -> {
                                        entityValue = Integer.parseInt(entityContent.substring(2), 16)
                                    }
                                    else -> {
                                        entityValue = Integer.parseInt(entityContent.substring(1), 10)
                                    }
                                }
                                if (entityValue > 0xFFFF) {
                                    entityValue = -1
                                }
                            } catch (e: NumberFormatException) {
                                entityValue = -1
                            }

                        }
                    } else { // escaped value content is an entity name
                        entityValue = this.entityValue(entityContent)
                    }
                }

                if (entityValue == -1) {
                    writer.write('&')
                    writer.write(entityContent)
                    writer.write(';')
                } else {
                    writer.write(entityValue)
                }
                i = semiColonIdx // move index up to the semi-colon
            } else {
                writer.write(c.toInt())
            }
            i++
        }
    }

    companion object {

        private val BASIC_ARRAY = arrayOf(arrayOf("quot", "34"), // " - double-quote
                arrayOf("amp", "38"), // & - ampersand
                arrayOf("lt", "60"), // < - less-than
                arrayOf("gt", "62"))// > - greater-than

        private val APOS_ARRAY = arrayOf(arrayOf("apos", "39"))// XML apostrophe

        // package scoped for testing
        internal val ISO8859_1_ARRAY = arrayOf(arrayOf("nbsp", "160"), // non-breaking space
                arrayOf("iexcl", "161"), // inverted exclamation mark
                arrayOf("cent", "162"), // cent sign
                arrayOf("pound", "163"), // pound sign
                arrayOf("curren", "164"), // currency sign
                arrayOf("yen", "165"), // yen sign = yuan sign
                arrayOf("brvbar", "166"), // broken bar = broken vertical bar
                arrayOf("sect", "167"), // section sign
                arrayOf("uml", "168"), // diaeresis = spacing diaeresis
                arrayOf("copy", "169"), // � - copyright sign
                arrayOf("ordf", "170"), // feminine ordinal indicator
                arrayOf("laquo", "171"), // left-pointing double angle quotation mark = left pointing guillemet
                arrayOf("not", "172"), // not sign
                arrayOf("shy", "173"), // soft hyphen = discretionary hyphen
                arrayOf("reg", "174"), // � - registered trademark sign
                arrayOf("macr", "175"), // macron = spacing macron = overline = APL overbar
                arrayOf("deg", "176"), // degree sign
                arrayOf("plusmn", "177"), // plus-minus sign = plus-or-minus sign
                arrayOf("sup2", "178"), // superscript two = superscript digit two = squared
                arrayOf("sup3", "179"), // superscript three = superscript digit three = cubed
                arrayOf("acute", "180"), // acute accent = spacing acute
                arrayOf("micro", "181"), // micro sign
                arrayOf("para", "182"), // pilcrow sign = paragraph sign
                arrayOf("middot", "183"), // middle dot = Georgian comma = Greek middle dot
                arrayOf("cedil", "184"), // cedilla = spacing cedilla
                arrayOf("sup1", "185"), // superscript one = superscript digit one
                arrayOf("ordm", "186"), // masculine ordinal indicator
                arrayOf("raquo", "187"), // right-pointing double angle quotation mark = right pointing guillemet
                arrayOf("frac14", "188"), // vulgar fraction one quarter = fraction one quarter
                arrayOf("frac12", "189"), // vulgar fraction one half = fraction one half
                arrayOf("frac34", "190"), // vulgar fraction three quarters = fraction three quarters
                arrayOf("iquest", "191"), // inverted question mark = turned question mark
                arrayOf("Agrave", "192"), // � - uppercase A, grave accent
                arrayOf("Aacute", "193"), // � - uppercase A, acute accent
                arrayOf("Acirc", "194"), // � - uppercase A, circumflex accent
                arrayOf("Atilde", "195"), // � - uppercase A, tilde
                arrayOf("Auml", "196"), // � - uppercase A, umlaut
                arrayOf("Aring", "197"), // � - uppercase A, ring
                arrayOf("AElig", "198"), // � - uppercase AE
                arrayOf("Ccedil", "199"), // � - uppercase C, cedilla
                arrayOf("Egrave", "200"), // � - uppercase E, grave accent
                arrayOf("Eacute", "201"), // � - uppercase E, acute accent
                arrayOf("Ecirc", "202"), // � - uppercase E, circumflex accent
                arrayOf("Euml", "203"), // � - uppercase E, umlaut
                arrayOf("Igrave", "204"), // � - uppercase I, grave accent
                arrayOf("Iacute", "205"), // � - uppercase I, acute accent
                arrayOf("Icirc", "206"), // � - uppercase I, circumflex accent
                arrayOf("Iuml", "207"), // � - uppercase I, umlaut
                arrayOf("ETH", "208"), // � - uppercase Eth, Icelandic
                arrayOf("Ntilde", "209"), // � - uppercase N, tilde
                arrayOf("Ograve", "210"), // � - uppercase O, grave accent
                arrayOf("Oacute", "211"), // � - uppercase O, acute accent
                arrayOf("Ocirc", "212"), // � - uppercase O, circumflex accent
                arrayOf("Otilde", "213"), // � - uppercase O, tilde
                arrayOf("Ouml", "214"), // � - uppercase O, umlaut
                arrayOf("times", "215"), // multiplication sign
                arrayOf("Oslash", "216"), // � - uppercase O, slash
                arrayOf("Ugrave", "217"), // � - uppercase U, grave accent
                arrayOf("Uacute", "218"), // � - uppercase U, acute accent
                arrayOf("Ucirc", "219"), // � - uppercase U, circumflex accent
                arrayOf("Uuml", "220"), // � - uppercase U, umlaut
                arrayOf("Yacute", "221"), // � - uppercase Y, acute accent
                arrayOf("THORN", "222"), // � - uppercase THORN, Icelandic
                arrayOf("szlig", "223"), // � - lowercase sharps, German
                arrayOf("agrave", "224"), // � - lowercase a, grave accent
                arrayOf("aacute", "225"), // � - lowercase a, acute accent
                arrayOf("acirc", "226"), // � - lowercase a, circumflex accent
                arrayOf("atilde", "227"), // � - lowercase a, tilde
                arrayOf("auml", "228"), // � - lowercase a, umlaut
                arrayOf("aring", "229"), // � - lowercase a, ring
                arrayOf("aelig", "230"), // � - lowercase ae
                arrayOf("ccedil", "231"), // � - lowercase c, cedilla
                arrayOf("egrave", "232"), // � - lowercase e, grave accent
                arrayOf("eacute", "233"), // � - lowercase e, acute accent
                arrayOf("ecirc", "234"), // � - lowercase e, circumflex accent
                arrayOf("euml", "235"), // � - lowercase e, umlaut
                arrayOf("igrave", "236"), // � - lowercase i, grave accent
                arrayOf("iacute", "237"), // � - lowercase i, acute accent
                arrayOf("icirc", "238"), // � - lowercase i, circumflex accent
                arrayOf("iuml", "239"), // � - lowercase i, umlaut
                arrayOf("eth", "240"), // � - lowercase eth, Icelandic
                arrayOf("ntilde", "241"), // � - lowercase n, tilde
                arrayOf("ograve", "242"), // � - lowercase o, grave accent
                arrayOf("oacute", "243"), // � - lowercase o, acute accent
                arrayOf("ocirc", "244"), // � - lowercase o, circumflex accent
                arrayOf("otilde", "245"), // � - lowercase o, tilde
                arrayOf("ouml", "246"), // � - lowercase o, umlaut
                arrayOf("divide", "247"), // division sign
                arrayOf("oslash", "248"), // � - lowercase o, slash
                arrayOf("ugrave", "249"), // � - lowercase u, grave accent
                arrayOf("uacute", "250"), // � - lowercase u, acute accent
                arrayOf("ucirc", "251"), // � - lowercase u, circumflex accent
                arrayOf("uuml", "252"), // � - lowercase u, umlaut
                arrayOf("yacute", "253"), // � - lowercase y, acute accent
                arrayOf("thorn", "254"), // � - lowercase thorn, Icelandic
                arrayOf("yuml", "255"))// � - lowercase y, umlaut

        // http://www.w3.org/TR/REC-html40/sgml/entities.html
        // package scoped for testing
        internal val HTML40_ARRAY = arrayOf(
                // <!-- Latin Extended-B -->
                arrayOf("fnof", "402"), // latin small f with hook = function= florin, U+0192 ISOtech -->
                // <!-- Greek -->
                arrayOf("Alpha", "913"), // greek capital letter alpha, U+0391 -->
                arrayOf("Beta", "914"), // greek capital letter beta, U+0392 -->
                arrayOf("Gamma", "915"), // greek capital letter gamma,U+0393 ISOgrk3 -->
                arrayOf("Delta", "916"), // greek capital letter delta,U+0394 ISOgrk3 -->
                arrayOf("Epsilon", "917"), // greek capital letter epsilon, U+0395 -->
                arrayOf("Zeta", "918"), // greek capital letter zeta, U+0396 -->
                arrayOf("Eta", "919"), // greek capital letter eta, U+0397 -->
                arrayOf("Theta", "920"), // greek capital letter theta,U+0398 ISOgrk3 -->
                arrayOf("Iota", "921"), // greek capital letter iota, U+0399 -->
                arrayOf("Kappa", "922"), // greek capital letter kappa, U+039A -->
                arrayOf("Lambda", "923"), // greek capital letter lambda,U+039B ISOgrk3 -->
                arrayOf("Mu", "924"), // greek capital letter mu, U+039C -->
                arrayOf("Nu", "925"), // greek capital letter nu, U+039D -->
                arrayOf("Xi", "926"), // greek capital letter xi, U+039E ISOgrk3 -->
                arrayOf("Omicron", "927"), // greek capital letter omicron, U+039F -->
                arrayOf("Pi", "928"), // greek capital letter pi, U+03A0 ISOgrk3 -->
                arrayOf("Rho", "929"), // greek capital letter rho, U+03A1 -->
                // <!-- there is no Sigmaf, and no U+03A2 character either -->
                arrayOf("Sigma", "931"), // greek capital letter sigma,U+03A3 ISOgrk3 -->
                arrayOf("Tau", "932"), // greek capital letter tau, U+03A4 -->
                arrayOf("Upsilon", "933"), // greek capital letter upsilon,U+03A5 ISOgrk3 -->
                arrayOf("Phi", "934"), // greek capital letter phi,U+03A6 ISOgrk3 -->
                arrayOf("Chi", "935"), // greek capital letter chi, U+03A7 -->
                arrayOf("Psi", "936"), // greek capital letter psi,U+03A8 ISOgrk3 -->
                arrayOf("Omega", "937"), // greek capital letter omega,U+03A9 ISOgrk3 -->
                arrayOf("alpha", "945"), // greek small letter alpha,U+03B1 ISOgrk3 -->
                arrayOf("beta", "946"), // greek small letter beta, U+03B2 ISOgrk3 -->
                arrayOf("gamma", "947"), // greek small letter gamma,U+03B3 ISOgrk3 -->
                arrayOf("delta", "948"), // greek small letter delta,U+03B4 ISOgrk3 -->
                arrayOf("epsilon", "949"), // greek small letter epsilon,U+03B5 ISOgrk3 -->
                arrayOf("zeta", "950"), // greek small letter zeta, U+03B6 ISOgrk3 -->
                arrayOf("eta", "951"), // greek small letter eta, U+03B7 ISOgrk3 -->
                arrayOf("theta", "952"), // greek small letter theta,U+03B8 ISOgrk3 -->
                arrayOf("iota", "953"), // greek small letter iota, U+03B9 ISOgrk3 -->
                arrayOf("kappa", "954"), // greek small letter kappa,U+03BA ISOgrk3 -->
                arrayOf("lambda", "955"), // greek small letter lambda,U+03BB ISOgrk3 -->
                arrayOf("mu", "956"), // greek small letter mu, U+03BC ISOgrk3 -->
                arrayOf("nu", "957"), // greek small letter nu, U+03BD ISOgrk3 -->
                arrayOf("xi", "958"), // greek small letter xi, U+03BE ISOgrk3 -->
                arrayOf("omicron", "959"), // greek small letter omicron, U+03BF NEW -->
                arrayOf("pi", "960"), // greek small letter pi, U+03C0 ISOgrk3 -->
                arrayOf("rho", "961"), // greek small letter rho, U+03C1 ISOgrk3 -->
                arrayOf("sigmaf", "962"), // greek small letter final sigma,U+03C2 ISOgrk3 -->
                arrayOf("sigma", "963"), // greek small letter sigma,U+03C3 ISOgrk3 -->
                arrayOf("tau", "964"), // greek small letter tau, U+03C4 ISOgrk3 -->
                arrayOf("upsilon", "965"), // greek small letter upsilon,U+03C5 ISOgrk3 -->
                arrayOf("phi", "966"), // greek small letter phi, U+03C6 ISOgrk3 -->
                arrayOf("chi", "967"), // greek small letter chi, U+03C7 ISOgrk3 -->
                arrayOf("psi", "968"), // greek small letter psi, U+03C8 ISOgrk3 -->
                arrayOf("omega", "969"), // greek small letter omega,U+03C9 ISOgrk3 -->
                arrayOf("thetasym", "977"), // greek small letter theta symbol,U+03D1 NEW -->
                arrayOf("upsih", "978"), // greek upsilon with hook symbol,U+03D2 NEW -->
                arrayOf("piv", "982"), // greek pi symbol, U+03D6 ISOgrk3 -->
                // <!-- General Punctuation -->
                arrayOf("bull", "8226"), // bullet = black small circle,U+2022 ISOpub -->
                // <!-- bullet is NOT the same as bullet operator, U+2219 -->
                arrayOf("hellip", "8230"), // horizontal ellipsis = three dot leader,U+2026 ISOpub -->
                arrayOf("prime", "8242"), // prime = minutes = feet, U+2032 ISOtech -->
                arrayOf("Prime", "8243"), // double prime = seconds = inches,U+2033 ISOtech -->
                arrayOf("oline", "8254"), // overline = spacing overscore,U+203E NEW -->
                arrayOf("frasl", "8260"), // fraction slash, U+2044 NEW -->
                // <!-- Letterlike Symbols -->
                arrayOf("weierp", "8472"), // script capital P = power set= Weierstrass p, U+2118 ISOamso -->
                arrayOf("image", "8465"), // blackletter capital I = imaginary part,U+2111 ISOamso -->
                arrayOf("real", "8476"), // blackletter capital R = real part symbol,U+211C ISOamso -->
                arrayOf("trade", "8482"), // trade mark sign, U+2122 ISOnum -->
                arrayOf("alefsym", "8501"), // alef symbol = first transfinite cardinal,U+2135 NEW -->
                // <!-- alef symbol is NOT the same as hebrew letter alef,U+05D0 although the
                // same glyph could be used to depict both characters -->
                // <!-- Arrows -->
                arrayOf("larr", "8592"), // leftwards arrow, U+2190 ISOnum -->
                arrayOf("uarr", "8593"), // upwards arrow, U+2191 ISOnum-->
                arrayOf("rarr", "8594"), // rightwards arrow, U+2192 ISOnum -->
                arrayOf("darr", "8595"), // downwards arrow, U+2193 ISOnum -->
                arrayOf("harr", "8596"), // left right arrow, U+2194 ISOamsa -->
                arrayOf("crarr", "8629"), // downwards arrow with corner leftwards= carriage return, U+21B5 NEW -->
                arrayOf("lArr", "8656"), // leftwards double arrow, U+21D0 ISOtech -->
                // <!-- ISO 10646 does not say that lArr is the same as the 'is implied by'
                // arrow but also does not have any other character for that function.
                // So ? lArr canbe used for 'is implied by' as ISOtech suggests -->
                arrayOf("uArr", "8657"), // upwards double arrow, U+21D1 ISOamsa -->
                arrayOf("rArr", "8658"), // rightwards double arrow,U+21D2 ISOtech -->
                // <!-- ISO 10646 does not say this is the 'implies' character but does not
                // have another character with this function so ?rArr can be used for
                // 'implies' as ISOtech suggests -->
                arrayOf("dArr", "8659"), // downwards double arrow, U+21D3 ISOamsa -->
                arrayOf("hArr", "8660"), // left right double arrow,U+21D4 ISOamsa -->
                // <!-- Mathematical Operators -->
                arrayOf("forall", "8704"), // for all, U+2200 ISOtech -->
                arrayOf("part", "8706"), // partial differential, U+2202 ISOtech -->
                arrayOf("exist", "8707"), // there exists, U+2203 ISOtech -->
                arrayOf("empty", "8709"), // empty set = null set = diameter,U+2205 ISOamso -->
                arrayOf("nabla", "8711"), // nabla = backward difference,U+2207 ISOtech -->
                arrayOf("isin", "8712"), // element of, U+2208 ISOtech -->
                arrayOf("notin", "8713"), // not an element of, U+2209 ISOtech -->
                arrayOf("ni", "8715"), // contains as member, U+220B ISOtech -->
                // <!-- should there be a more memorable name than 'ni'? -->
                arrayOf("prod", "8719"), // n-ary product = product sign,U+220F ISOamsb -->
                // <!-- prod is NOT the same character as U+03A0 'greek capital letter pi'
                // though the same glyph might be used for both -->
                arrayOf("sum", "8721"), // n-ary summation, U+2211 ISOamsb -->
                // <!-- sum is NOT the same character as U+03A3 'greek capital letter sigma'
                // though the same glyph might be used for both -->
                arrayOf("minus", "8722"), // minus sign, U+2212 ISOtech -->
                arrayOf("lowast", "8727"), // asterisk operator, U+2217 ISOtech -->
                arrayOf("radic", "8730"), // square root = radical sign,U+221A ISOtech -->
                arrayOf("prop", "8733"), // proportional to, U+221D ISOtech -->
                arrayOf("infin", "8734"), // infinity, U+221E ISOtech -->
                arrayOf("ang", "8736"), // angle, U+2220 ISOamso -->
                arrayOf("and", "8743"), // logical and = wedge, U+2227 ISOtech -->
                arrayOf("or", "8744"), // logical or = vee, U+2228 ISOtech -->
                arrayOf("cap", "8745"), // intersection = cap, U+2229 ISOtech -->
                arrayOf("cup", "8746"), // union = cup, U+222A ISOtech -->
                arrayOf("int", "8747"), // integral, U+222B ISOtech -->
                arrayOf("there4", "8756"), // therefore, U+2234 ISOtech -->
                arrayOf("sim", "8764"), // tilde operator = varies with = similar to,U+223C ISOtech -->
                // <!-- tilde operator is NOT the same character as the tilde, U+007E,although
                // the same glyph might be used to represent both -->
                arrayOf("cong", "8773"), // approximately equal to, U+2245 ISOtech -->
                arrayOf("asymp", "8776"), // almost equal to = asymptotic to,U+2248 ISOamsr -->
                arrayOf("ne", "8800"), // not equal to, U+2260 ISOtech -->
                arrayOf("equiv", "8801"), // identical to, U+2261 ISOtech -->
                arrayOf("le", "8804"), // less-than or equal to, U+2264 ISOtech -->
                arrayOf("ge", "8805"), // greater-than or equal to,U+2265 ISOtech -->
                arrayOf("sub", "8834"), // subset of, U+2282 ISOtech -->
                arrayOf("sup", "8835"), // superset of, U+2283 ISOtech -->
                // <!-- note that nsup, 'not a superset of, U+2283' is not covered by the
                // Symbol font encoding and is not included. Should it be, for symmetry?
                // It is in ISOamsn --> <!ENTITY nsub", "8836"},
                // not a subset of, U+2284 ISOamsn -->
                arrayOf("sube", "8838"), // subset of or equal to, U+2286 ISOtech -->
                arrayOf("supe", "8839"), // superset of or equal to,U+2287 ISOtech -->
                arrayOf("oplus", "8853"), // circled plus = direct sum,U+2295 ISOamsb -->
                arrayOf("otimes", "8855"), // circled times = vector product,U+2297 ISOamsb -->
                arrayOf("perp", "8869"), // up tack = orthogonal to = perpendicular,U+22A5 ISOtech -->
                arrayOf("sdot", "8901"), // dot operator, U+22C5 ISOamsb -->
                // <!-- dot operator is NOT the same character as U+00B7 middle dot -->
                // <!-- Miscellaneous Technical -->
                arrayOf("lceil", "8968"), // left ceiling = apl upstile,U+2308 ISOamsc -->
                arrayOf("rceil", "8969"), // right ceiling, U+2309 ISOamsc -->
                arrayOf("lfloor", "8970"), // left floor = apl downstile,U+230A ISOamsc -->
                arrayOf("rfloor", "8971"), // right floor, U+230B ISOamsc -->
                arrayOf("lang", "9001"), // left-pointing angle bracket = bra,U+2329 ISOtech -->
                // <!-- lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation
                // mark' -->
                arrayOf("rang", "9002"), // right-pointing angle bracket = ket,U+232A ISOtech -->
                // <!-- rang is NOT the same character as U+003E 'greater than' or U+203A
                // 'single right-pointing angle quotation mark' -->
                // <!-- Geometric Shapes -->
                arrayOf("loz", "9674"), // lozenge, U+25CA ISOpub -->
                // <!-- Miscellaneous Symbols -->
                arrayOf("spades", "9824"), // black spade suit, U+2660 ISOpub -->
                // <!-- black here seems to mean filled as opposed to hollow -->
                arrayOf("clubs", "9827"), // black club suit = shamrock,U+2663 ISOpub -->
                arrayOf("hearts", "9829"), // black heart suit = valentine,U+2665 ISOpub -->
                arrayOf("diams", "9830"), // black diamond suit, U+2666 ISOpub -->

                // <!-- Latin Extended-A -->
                arrayOf("OElig", "338"), // -- latin capital ligature OE,U+0152 ISOlat2 -->
                arrayOf("oelig", "339"), // -- latin small ligature oe, U+0153 ISOlat2 -->
                // <!-- ligature is a misnomer, this is a separate character in some languages -->
                arrayOf("Scaron", "352"), // -- latin capital letter S with caron,U+0160 ISOlat2 -->
                arrayOf("scaron", "353"), // -- latin small letter s with caron,U+0161 ISOlat2 -->
                arrayOf("Yuml", "376"), // -- latin capital letter Y with diaeresis,U+0178 ISOlat2 -->
                // <!-- Spacing Modifier Letters -->
                arrayOf("circ", "710"), // -- modifier letter circumflex accent,U+02C6 ISOpub -->
                arrayOf("tilde", "732"), // small tilde, U+02DC ISOdia -->
                // <!-- General Punctuation -->
                arrayOf("ensp", "8194"), // en space, U+2002 ISOpub -->
                arrayOf("emsp", "8195"), // em space, U+2003 ISOpub -->
                arrayOf("thinsp", "8201"), // thin space, U+2009 ISOpub -->
                arrayOf("zwnj", "8204"), // zero width non-joiner,U+200C NEW RFC 2070 -->
                arrayOf("zwj", "8205"), // zero width joiner, U+200D NEW RFC 2070 -->
                arrayOf("lrm", "8206"), // left-to-right mark, U+200E NEW RFC 2070 -->
                arrayOf("rlm", "8207"), // right-to-left mark, U+200F NEW RFC 2070 -->
                arrayOf("ndash", "8211"), // en dash, U+2013 ISOpub -->
                arrayOf("mdash", "8212"), // em dash, U+2014 ISOpub -->
                arrayOf("lsquo", "8216"), // left single quotation mark,U+2018 ISOnum -->
                arrayOf("rsquo", "8217"), // right single quotation mark,U+2019 ISOnum -->
                arrayOf("sbquo", "8218"), // single low-9 quotation mark, U+201A NEW -->
                arrayOf("ldquo", "8220"), // left double quotation mark,U+201C ISOnum -->
                arrayOf("rdquo", "8221"), // right double quotation mark,U+201D ISOnum -->
                arrayOf("bdquo", "8222"), // double low-9 quotation mark, U+201E NEW -->
                arrayOf("dagger", "8224"), // dagger, U+2020 ISOpub -->
                arrayOf("Dagger", "8225"), // double dagger, U+2021 ISOpub -->
                arrayOf("permil", "8240"), // per mille sign, U+2030 ISOtech -->
                arrayOf("lsaquo", "8249"), // single left-pointing angle quotation mark,U+2039 ISO proposed -->
                // <!-- lsaquo is proposed but not yet ISO standardized -->
                arrayOf("rsaquo", "8250"), // single right-pointing angle quotation mark,U+203A ISO proposed -->
                // <!-- rsaquo is proposed but not yet ISO standardized -->
                arrayOf("euro", "8364"))// -- euro sign, U+20AC NEW -->

        /**
         *
         *
         * The set of entities supported by standard XML.
         *
         */
        val XML: Entities

        /**
         *
         *
         * The set of entities supported by HTML 3.2.
         *
         */
        val HTML32: Entities

        /**
         *
         *
         * The set of entities supported by HTML 4.0.
         *
         */
        val HTML40: Entities

        init {
            XML = Entities()
            XML.addEntities(BASIC_ARRAY)
            XML.addEntities(APOS_ARRAY)
        }

        init {
            HTML32 = Entities()
            HTML32.addEntities(BASIC_ARRAY)
            HTML32.addEntities(ISO8859_1_ARRAY)
        }

        init {
            HTML40 = Entities()
            fillWithHtml40Entities(HTML40)
        }

        /**
         *
         *
         * Fills the specified entities instance with HTML 40 entities.
         *
         *
         * @param entities the instance to be filled.
         */
        internal fun fillWithHtml40Entities(entities: Entities) {
            entities.addEntities(BASIC_ARRAY)
            entities.addEntities(ISO8859_1_ARRAY)
            entities.addEntities(HTML40_ARRAY)
        }
    }

}
