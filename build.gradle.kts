// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}
// Load secrets.properties
val secretsFile = rootProject.file("secrets.properties")
val secretsProperties = java.util.Properties()
if (secretsFile.exists()) {
    secretsProperties.load(java.io.FileInputStream(secretsFile))
}

// Make properties available to all modules
ext {
    set("MAPS_API_KEY", secretsProperties.getProperty("MAPS_API_KEY", ""))
}