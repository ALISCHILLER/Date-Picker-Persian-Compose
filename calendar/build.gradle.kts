import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.msa.calendar"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        checkDependencies = true
        htmlReport = true
        xmlReport = true
        sarifReport = true
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel2Api30") {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp"
                }
            }
        }
    }

    packaging {
        resources {
            // Preserve license and notice material instead of silently removing attribution.
            merges += setOf(
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
            )
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

kotlin {
    explicitApiWarning()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    api(project(":calendar-core"))
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.annotation)
    implementation(libs.kotlinx.coroutines.core)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}


val libraryGroup = providers.gradleProperty("LIBRARY_GROUP").get()
val libraryArtifact = providers.gradleProperty("LIBRARY_COMPOSE_ARTIFACT").get()
val libraryVersion = providers.gradleProperty("LIBRARY_VERSION").get()
val skipSigning = providers.gradleProperty("skipSigning")
    .map { it.toBoolean() }
    .getOrElse(false)

group = libraryGroup
version = libraryVersion

mavenPublishing {
    coordinates(libraryGroup, libraryArtifact, libraryVersion)

    publishToMavenCentral()

    if (!skipSigning) {
        signAllPublications()
    }

    pom {
        name.set("Persian Date Picker Compose")
        description.set(
            "A customizable Jalali/Persian date and date-range picker " +
                "for Android Jetpack Compose.",
        )
        inceptionYear.set("2023")
        url.set("https://github.com/ALISCHILLER/Date-Picker-Persian-Compose")

        licenses {
            license {
                name.set("GNU Affero General Public License v3.0 only")
                url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("ALISCHILLER")
                name.set("Ali Soleimani")
                url.set("https://github.com/ALISCHILLER")
            }
        }

        scm {
            url.set("https://github.com/ALISCHILLER/Date-Picker-Persian-Compose")
            connection.set(
                "scm:git:git://github.com/ALISCHILLER/Date-Picker-Persian-Compose.git",
            )
            developerConnection.set(
                "scm:git:ssh://git@github.com/ALISCHILLER/Date-Picker-Persian-Compose.git",
            )
        }
    }
}
