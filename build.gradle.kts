import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

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
        google()
        jcenter()
    }

    dependencies {
        classpath(PaperWalaConfig.Plugins.androidPlugin)
        classpath(PaperWalaConfig.Plugins.kotlinPlugin)
    }

}



allprojects {
    repositories {
        google()
        jcenter()
        mavenLocal()
    }

    if ((group as String).isNotEmpty()) {
        conFigureAndroid()
    }
}

fun Project.conFigureAndroid() {
    apply(plugin = "com.android.application")
    apply(plugin = "kotlin-android")
    apply(plugin = "kotlin-android-extensions")
    apply(plugin = "kotlin-kapt")

    configure<BaseExtension> {
        compileSdkVersion(PaperWalaConfig.SdkVersion.compile)

        defaultConfig {
            minSdkVersion(PaperWalaConfig.SdkVersion.min)
            targetSdkVersion(PaperWalaConfig.SdkVersion.target)
            versionCode = 1
            versionName = PaperWalaConfig.version
        }
    }

    configure<KotlinProjectExtension> {
        experimental.coroutines = Coroutines.ENABLE
    }
}