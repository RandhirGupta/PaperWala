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

package com.cyborg.paperwala.di

import android.content.Context
import com.cyborg.paperwala.data.local.LocalSharedPrefs
import com.cyborg.paperwala.data.local.LocalSource
import com.cyborg.paperwala.data.local.LocalSourceImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocalModule {

//    @Provides
//    fun provideDatabase(context: Context): LocalDatabase {
//        return Room.databaseBuilder(context, LocalDatabase::class.java, "${context.getString(R.string.app_name)}.db").build()
//    }

    @Provides
    fun provideSharedPrefs(context: Context): LocalSharedPrefs {
        return LocalSharedPrefs(context)
    }

    @Singleton
    @Provides
    fun provideLocalSource(/*localDatabase: LocalDatabase, */localSharedPrefs: LocalSharedPrefs): LocalSource {
        return LocalSourceImpl(/*localDatabase,*/ localSharedPrefs)
    }
}