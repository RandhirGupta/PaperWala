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