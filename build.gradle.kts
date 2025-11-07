// Top-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    // 👇 Add this line to enable Google services globally
    id("com.google.gms.google-services") version "4.4.2" apply false
}
