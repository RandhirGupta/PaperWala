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

android {
    defaultConfig {
        applicationId = "com.cyborg.paperwala"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        ndkBuild {
            setPath("src/main/jni/Android.mk")
        }
    }

    dataBinding {
        isEnabled = true
    }
}

dependencies {
    implementation(PaperWalaConfig.Libs.Kotlin.jvm)
    implementation(PaperWalaConfig.Libs.Kotlin.coroutineAndroid)

    implementation(PaperWalaConfig.Libs.Support.appCompat)
    implementation(PaperWalaConfig.Libs.Support.design)
    implementation(PaperWalaConfig.Libs.Support.constraintLayout)
    implementation(PaperWalaConfig.Libs.Support.multidex)
    implementation(PaperWalaConfig.Libs.Support.annotations)
    implementation(PaperWalaConfig.Libs.Support.materialDesign)


    implementation(PaperWalaConfig.Libs.Arch.lifeCycle)
    implementation(PaperWalaConfig.Libs.Arch.room)

    implementation(PaperWalaConfig.Libs.Rx.rxJava)
    implementation(PaperWalaConfig.Libs.Rx.rxKotlin)
    implementation(PaperWalaConfig.Libs.Rx.rxAndroid)

    implementation(PaperWalaConfig.Libs.Dagger.daggerAndroid)
    implementation(PaperWalaConfig.Libs.Dagger.daggerAndroidSupport)

    implementation(PaperWalaConfig.Libs.Misc.retrofit)
    implementation(PaperWalaConfig.Libs.Misc.retrofitGson)
    implementation(PaperWalaConfig.Libs.Misc.rxRetrofitAdater)
    implementation(PaperWalaConfig.Libs.Misc.okHttpInterceptor)
    implementation(PaperWalaConfig.Libs.Misc.glide)
    implementation(PaperWalaConfig.Libs.Misc.retrofitAdapter)
    debugImplementation(PaperWalaConfig.Libs.Misc.dbDebug)

    kapt(PaperWalaConfig.Libs.Dagger.daggerCompiler)
    kapt(PaperWalaConfig.Libs.Dagger.daggerAndroidCompiler)
    kapt(PaperWalaConfig.Libs.Arch.roomCompiler)

    testImplementation(PaperWalaConfig.Libs.Test.junit)
    testImplementation(PaperWalaConfig.Libs.Arch.roomTestHelper)
    testImplementation(PaperWalaConfig.Libs.Test.Mockito.nhaarmanMock)
    testImplementation(PaperWalaConfig.Libs.Misc.retrofitMock)

    androidTestImplementation(PaperWalaConfig.Libs.AndroidTest.testRunner)
    androidTestImplementation(PaperWalaConfig.Libs.AndroidTest.espressoCore)
}