/*
 * Copyright 2019 randhirgupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyborg.paperwala.data.local

import android.content.Context
import android.content.SharedPreferences
import com.cyborg.paperwala.R

class LocalSharedPrefs(context: Context) {

    private var editor: SharedPreferences.Editor? = null
    private val settings: SharedPreferences? = context.getSharedPreferences(context.getString(R.string.app_name).toUpperCase(), Context.MODE_PRIVATE)

    fun set(name: String, defaultValue: Long) {
        editor = settings?.edit()
        editor?.putLong(name, defaultValue)
        editor?.apply()
    }

    fun get(name: String, defaultValue: Long): Long? = settings?.getLong(name, defaultValue)
}