package me.angrybyte.goose.outputformatters

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

/*
 * Note: originally released under the GNU LGPL v2.1,
 * but rereleased by the original author under the ASF license (above).
 */

/**
 *
 *
 * A hash map that uses primitive ints for the key rather than objects.
 *
 *
 *
 *
 *
 * Note that this class is for internal optimization purposes only, and may not be supported in future releases of Apache Commons Lang.
 * Utilities of this sort may be included in future releases of Apache Commons Collections.
 *
 *
 * @author Apache Software Foundation
 * @author Justin Couch
 * @author Alex Chaffee (alex@apache.org)
 * @version $Revision: 905857 $
 * @see java.util.HashMap
 *
 * @since 2.0
 */
class IntHashMap
/**
 *
 *
 * Constructs a new, empty hashtable with the specified initial capacity and the specified load factor.
 *
 *
 * @param initialCapacity the initial capacity of the hashtable.
 * @param loadFactor the load factor of the hashtable.
 * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load factor is nonpositive.
 */
@JvmOverloads constructor(initialCapacity: Int = 20,
                          /**
                           * The load factor for the hashtable.
                           *
                           * @serial
                           */
                          private val loadFactor: Float = 0.75f) {

    /**
     * The hash table data.
     */
    @Transient
    private var table: Array<Entry>? = null

    /**
     * The total number of entries in the hash table.
     */
    @Transient
    private var count: Int = 0

    /**
     * The table is rehashed when its size exceeds this threshold. (The value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */
    private var threshold: Int = 0

    /**
     *
     *
     * Tests if this hashtable maps no keys to values.
     *
     *
     * @return `true` if this hashtable maps no keys to values; `false` otherwise.
     */
    val isEmpty: Boolean
        get() = count == 0

    /**
     *
     *
     * Innerclass that acts as a datastructure to create a new entry in the table.
     *
     */
    private class Entry
    /**
     *
     *
     * Create a new entry with the given values.
     *
     *
     * @param hash The code used to hash the object with
     * @param key The key used to enter this in the table
     * @param value The value for this key
     * @param next A reference to the next entry in the table
     */
    (internal val hash: Int, internal val key: Int // TODO not read; seems to be always same as hash
     , internal var value: Any?, internal var next: Entry)

    init {
        var initialCapacity = initialCapacity
        if (initialCapacity < 0) {
            throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        }
        if (loadFactor <= 0) {
            throw IllegalArgumentException("Illegal Load: $loadFactor")
        }
        if (initialCapacity == 0) {
            initialCapacity = 1
        }
        table = arrayOfNulls(initialCapacity)
        threshold = (initialCapacity * loadFactor).toInt()
    }

    /**
     *
     *
     * Returns the number of keys in this hashtable.
     *
     *
     * @return the number of keys in this hashtable.
     */
    fun size(): Int {
        return count
    }

    /**
     *
     *
     * Tests if some key maps into the specified value in this hashtable. This operation is more expensive than the `containsKey`
     * method.
     *
     *
     *
     *
     *
     * Note that this method is identical in functionality to containsValue, (which is part of the Map interface in the collections
     * framework).
     *
     *
     * @param value a value to search for.
     * @return `true` if and only if some key maps to the `value` argument in this hashtable as determined by the
     * <tt>equals</tt> method; `false` otherwise.
     * @throws NullPointerException if the value is `null`.
     * @see .containsKey
     * @see .containsValue
     * @see java.util.Map
     */
    operator fun contains(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        val tab = table
        var i = tab!!.size
        while (i-- > 0) {
            var e: Entry? = tab[i]
            while (e != null) {
                if (e.value == value) {
                    return true
                }
                e = e.next
            }
        }
        return false
    }

    /**
     *
     *
     * Returns `true` if this HashMap maps one or more keys to this value.
     *
     *
     *
     *
     *
     * Note that this method is identical in functionality to contains (which predates the Map interface).
     *
     *
     * @param value value whose presence in this HashMap is to be tested.
     * @return boolean `true` if the value is contained
     * @see java.util.Map
     *
     * @since JDK1.2
     */
    fun containsValue(value: Any): Boolean {
        return contains(value)
    }

    /**
     *
     *
     * Tests if the specified object is a key in this hashtable.
     *
     *
     * @param key possible key.
     * @return `true` if and only if the specified object is a key in this hashtable, as determined by the <tt>equals</tt>
     * method; `false` otherwise.
     * @see .contains
     */
    fun containsKey(key: Int): Boolean {
        val tab = table
        val index = (key and 0x7FFFFFFF) % tab!!.size
        var e: Entry? = tab[index]
        while (e != null) {
            if (e.hash == key) {
                return true
            }
            e = e.next
        }
        return false
    }

    /**
     *
     *
     * Returns the value to which the specified key is mapped in this map.
     *
     *
     * @param key a key in the hashtable.
     * @return the value to which the key is mapped in this hashtable; `null` if the key is not mapped to any value in this
     * hashtable.
     * @see .put
     */
    operator fun get(key: Int): Any? {
        val tab = table
        val index = (key and 0x7FFFFFFF) % tab!!.size
        var e: Entry? = tab[index]
        while (e != null) {
            if (e.hash == key) {
                return e.value
            }
            e = e.next
        }
        return null
    }

    /**
     *
     *
     * Increases the capacity of and internally reorganizes this hashtable, in order to accommodate and access its entries more efficiently.
     *
     *
     *
     *
     *
     * This method is called automatically when the number of keys in the hashtable exceeds this hashtable's capacity and load factor.
     *
     */
    protected fun rehash() {
        val oldCapacity = table!!.size
        val oldMap = table

        val newCapacity = oldCapacity * 2 + 1
        val newMap = arrayOfNulls<Entry>(newCapacity)

        threshold = (newCapacity * loadFactor).toInt()
        table = newMap

        var i = oldCapacity
        while (i-- > 0) {
            var old: Entry? = oldMap!![i]
            while (old != null) {
                val e = old
                old = old.next

                val index = (e.hash and 0x7FFFFFFF) % newCapacity
                e.next = newMap[index]
                newMap[index] = e
            }
        }
    }

    /**
     *
     *
     * Maps the specified `key` to the specified `value` in this hashtable. The key cannot be `null`.
     *
     *
     *
     *
     *
     * The value can be retrieved by calling the `get` method with a key that is equal to the original key.
     *
     *
     * @param key the hashtable key.
     * @param value the value.
     * @return the previous value of the specified key in this hashtable, or `null` if it did not have one.
     * @throws NullPointerException if the key is `null`.
     * @see .get
     */
    fun put(key: Int, value: Any): Any? {
        // Makes sure the key is not already in the hashtable.
        var tab = table
        var index = (key and 0x7FFFFFFF) % tab!!.size
        run {
            var e: Entry? = tab!![index]
            while (e != null) {
                if (e.hash == key) {
                    val old = e.value
                    e.value = value
                    return old
                }
                e = e.next
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash()

            tab = table
            index = (key and 0x7FFFFFFF) % tab!!.size
        }

        // Creates the new entry.
        val e = Entry(key, key, value, tab[index])
        tab[index] = e
        count++
        return null
    }

    /**
     *
     *
     * Removes the key (and its corresponding value) from this hashtable.
     *
     *
     *
     *
     *
     * This method does nothing if the key is not present in the hashtable.
     *
     *
     * @param key the key that needs to be removed.
     * @return the value to which the key had been mapped in this hashtable, or `null` if the key did not have a mapping.
     */
    fun remove(key: Int): Any? {
        val tab = table
        val index = (key and 0x7FFFFFFF) % tab!!.size
        var e: Entry? = tab[index]
        var prev: Entry? = null
        while (e != null) {
            if (e.hash == key) {
                if (prev != null) {
                    prev.next = e.next
                } else {
                    tab[index] = e.next
                }
                count--
                val oldValue = e.value
                e.value = null
                return oldValue
            }
            prev = e
            e = e.next
        }
        return null
    }

    /**
     *
     *
     * Clears this hashtable so that it contains no keys.
     *
     */
    @Synchronized
    fun clear() {
        val tab = table
        var index = tab!!.size
        while (--index >= 0) {
            tab[index] = null
        }
        count = 0
    }

}
/**
 *
 *
 * Constructs a new, empty hashtable with a default capacity and load factor, which is `20` and `0.75`
 * respectively.
 *
 */
/**
 *
 *
 * Constructs a new, empty hashtable with the specified initial capacity and default load factor, which is `0.75`.
 *
 *
 * @param initialCapacity the initial capacity of the hashtable.
 * @throws IllegalArgumentException if the initial capacity is less than zero.
 */
