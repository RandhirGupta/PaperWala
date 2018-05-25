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

//group = "com.cyborg"
//version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        jcenter()
        maven { setUrl("https://kotlin.bintray.com/kotlinx") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://kotlin.bintray.com/kotlin-js-wrappers") }
        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
    }

    dependencies {
        classpath(PaperWalaConfig.BuildPlugin.androidPlugin)
        classpath(PaperWalaConfig.BuildPlugin.kotlinPlugin)
        classpath(PaperWalaConfig.BuildPlugin.kotlinSerialization)
        classpath(PaperWalaConfig.BuildPlugin.googlePlayPlugin)
    }

}



allprojects {
    repositories {
        jcenter()
        maven { setUrl("https://kotlin.bintray.com/kotlinx") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://kotlin.bintray.com/kotlin-js-wrappers") }
        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
    }
}