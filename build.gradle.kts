plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.cyclonedx)
}

// Root metadata is also used by the aggregate CycloneDX SBOM.
group = providers.gradleProperty("LIBRARY_GROUP").get()
version = providers.gradleProperty("LIBRARY_VERSION").get()
