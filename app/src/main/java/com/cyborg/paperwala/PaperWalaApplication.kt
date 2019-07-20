/*
 * Copyright 2018 randhirgupta
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

package com.cyborg.paperwala

import com.cyborg.paperwala.di.*
import com.cyborg.paperwala.di.component.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class PaperWalaApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder()
                .application(this)
                .applicationModule(ApplicationModule(applicationContext))
                .localModule(LocalModule())
                .networkModule(NetworkModule())
                .repositoryModule(RepositoryModule())
                .useCaseModule(UseCaseModule())
                .build()
    }
}