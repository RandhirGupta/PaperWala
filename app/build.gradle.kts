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

import org.jetbrains.kotlin.gradle.dsl.Coroutines
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(27)
    buildToolsVersion("27.0.3")

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(27)

        applicationId = "com.cyborg.paperwala"

        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }

    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }

}

dependencies {
    implementation(PaperWalaConfig.Libs.appCompatV7)
    implementation(PaperWalaConfig.Libs.recyclerView)
    implementation(PaperWalaConfig.Libs.cardView)
    implementation(PaperWalaConfig.Libs.designSupportLibs)
    implementation(PaperWalaConfig.Libs.constraintLayout)
    implementation(kotlin(PaperWalaConfig.KotlinModule.stdLib, PaperWalaConfig.kotlinVersion))
    implementation(PaperWalaConfig.Libs.kotlinXCoroutineAndroid)
    implementation(PaperWalaConfig.Libs.roomLibs)


    debugImplementation(PaperWalaConfig.Libs.debugDatabase)

    kapt(PaperWalaConfig.Libs.roomCompilerLibs)
}

repositories {
    jcenter()
    google()
    mavenCentral()
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

androidExtensions {
    isExperimental = true
}

//apply {
//    plugin("com.google.gms.google-services")
//}