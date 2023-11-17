// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    dependencies {
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
    }
    repositories {
        google()
    }
}// Top
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}