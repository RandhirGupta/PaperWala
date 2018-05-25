object PaperWalaConfig {

    const val kotlinVersion = "1.2.30"
    const val kotlinSerializationVersion = "0.4"
    const val kotlinXCoroutineVersion = "0.22.5"

    const val androidGradlePluginVersion = "3.0.0"
    const val playPluginVersion = "3.2.0"

    const val retrofitVersion = "2.4.0"
    const val okhttpVersion = "3.10.0"
    const val androidSupportLibsVersion = "27.0.1"
    const val constraintLayoutVersion = "1.0.2"
    const val archComponentVersion = "1.0.0"


    object BuildPlugin {
        val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin:$kotlinSerializationVersion"
        val androidPlugin = "com.android.tools.build:gradle:$androidGradlePluginVersion"
        val googlePlayPlugin = "com.google.gms:google-services:$playPluginVersion"
    }

    object KotlinModule {
        val stdLib = "stdlib"
        val commonStdLib = "stdlib-common"
        val jdk8Lib = "stdlib-jdk8"
        val jsLib = "stdlib-js"
    }

    object Libs {
        val serializationCommon = "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinSerializationVersion"
        val serializationJvm = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinSerializationVersion"

        val kotlinXCoroutineCommon = "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinXCoroutineVersion"
        val kotlinXCoroutineJvm = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXCoroutineVersion"
        val kotlinXCoroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinXCoroutineVersion"

        val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
        val retrofitGsonConv = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
        val retrofitCoroutineAdapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-experimental-adapter:1.0.0"
        val okhttpLoggerInterceptor = "com.squareup.okhttp3:logging-interceptor:$okhttpVersion"


        val appCompatV7 = "com.android.support:appcompat-v7:$androidSupportLibsVersion"
        val recyclerView = "com.android.support:recyclerview-v7:$androidSupportLibsVersion"
        val cardView = "com.android.support:cardview-v7:$androidSupportLibsVersion"
        val constraintLayout = "com.android.support.constraint:constraint-layout:$constraintLayoutVersion"
        val designSupportLibs = "com.android.support:design:$androidSupportLibsVersion"

        val roomLibs = "android.arch.persistence.room:runtime:$archComponentVersion"
        val roomCompilerLibs = "android.arch.persistence.room:compiler:$archComponentVersion"

        val debugDatabase = "com.amitshekhar.android:debug-db:1.0.3"
    }
}