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

object PaperWalaConfig {

    private const val kotlinVersion = "1.2.70"
    const val version = "1.0"

    object SdkVersion {
        const val compile = 28
        const val target = 28
        const val min = 21
    }


    object Plugins {
        const val androidPlugin = "com.android.tools.build:gradle:3.2.0"
        const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    }

    object Libs {
        object Kotlin {
            const val jvm = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
            const val coroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:0.30.0"
        }

        object Support {
            private const val buildToolVersion = "28.0.0"

            const val appCompat = "androidx.appcompat:appcompat:1.0.0-alpha1"
            const val design = "com.android.support:design:$buildToolVersion"
            const val constraitLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
            const val cardView = "com.android.support:cardview-v7:$buildToolVersion"
            const val multidex = "com.android.support:multidex:1.0.3"
            const val annotations = "com.android.support:support-annotations:$buildToolVersion"
            const val materialDesign = "com.google.android.material:material:1.0.0-alpha1"
            const val recyclerView = "androidx.recyclerview:recyclerview:1.0.0-alpha1"
        }

        object Arch {
            private const val lifeCycleVersion = "2.0.0-beta01"
            private const val roomVersion = "2.0.0-beta01"

            const val lifeCycle = "androidx.lifecycle:lifecycle-extensions:$lifeCycleVersion"
            const val lifeCycleTestHelper = "androidx.arch.core:core-testing:$lifeCycleVersion"
            const val room = "androidx.room:room-runtime:$roomVersion"
            const val roomCompiler = "androidx.room:room-compiler:$roomVersion" // use kapt for Kotlin
            const val roomRxJavaSupport = "androidx.room:room-rxjava2:$roomVersion"
            const val roomGuavaSupport = "androidx.room:room-guava:$roomVersion"
            const val roomTestHelper = "androidx.room:room-testing:$roomVersion"
        }

        object Misc {
            private const val retrofitVersion = "2.4.0"
            private const val glideVersion = "4.8.0"
            private const val okHttpVersion = "3.11.0"

            const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
            const val retrofitMock = "com.squareup.retrofit2:retrofit-mock:$retrofitVersion"
            const val retrofitGson = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
            const val okHttpInterceptor = "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
            const val glide = "com.github.bumptech.glide:glide:$glideVersion"
            const val glideCompiler = "com.github.bumptech.glide:compiler:$glideVersion"
            const val dbDebug = "com.amitshekhar.android:debug-db:1.0.4"
            const val jSoup = "org.jsoup:jsoup:1.10.3"
            const val retrofitAdapter = "com.squareup.retrofit2:adapter-rxjava:2.1.0"
            const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.0"
            const val rxJava = "io.reactivex.rxjava2:rxjava:2.2.4"
        }

        object Test {
            const val junit = "junit:junit:4.12"

            object Mockito {
                const val nhaarmanMock = "com.nhaarman:mockito-kotlin:1.6.0"
            }
        }

        object AndroidTest {
            const val testRunner = "androidx.test:runner:1.1.0-alpha3"
            const val espressoCore = "androidx.test.espresso:espresso-core:3.1.0-alpha3"
        }

        object Dagger {
            private const val daggerVersion = "2.16"

            const val daggerAndroid = "com.google.dagger:dagger-android:$daggerVersion"
            const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:$daggerVersion"

            const val daggerCompiler = "com.google.dagger:dagger-compiler:$daggerVersion"
            const val daggerAndroidCompiler = "com.google.dagger:dagger-android-processor:$daggerVersion"
        }
    }

}