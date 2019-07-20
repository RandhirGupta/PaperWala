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

import io.reactivex.Flowable
import javax.inject.Singleton

@Singleton
class LocalSourceImpl(/*private val localDatabase: LocalDatabase,*/ private val localSharedPrefs: LocalSharedPrefs) :
        LocalSource {

    override fun getNews(entryId: Long?): Flowable<List<String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}