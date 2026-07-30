import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)
    explicitApi()

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xconsistent-data-class-copy-visibility",
        )
    }
}

tasks.test {
    useJUnit()
}

dependencies {
    testImplementation(libs.junit)
}

ktlint {
    version.set(libs.versions.ktlint.engine)
    verbose.set(true)
    outputToConsole.set(true)
}

val libraryGroup = providers.gradleProperty("LIBRARY_GROUP").get()
val libraryArtifact = providers.gradleProperty("LIBRARY_CORE_ARTIFACT").get()
val libraryVersion = providers.gradleProperty("LIBRARY_VERSION").get()
val skipSigning = providers.gradleProperty("skipSigning")
    .map(String::toBoolean)
    .getOrElse(false)

group = libraryGroup
version = libraryVersion

mavenPublishing {
    coordinates(libraryGroup, libraryArtifact, libraryVersion)
    publishToMavenCentral()
    if (!skipSigning) signAllPublications()

    pom {
        name.set("Persian Calendar Core")
        description.set("Pure Kotlin/JVM Jalali calendar conversion, arithmetic, ranges, and digit utilities.")
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
            connection.set("scm:git:git://github.com/ALISCHILLER/Date-Picker-Persian-Compose.git")
            developerConnection.set("scm:git:ssh://git@github.com/ALISCHILLER/Date-Picker-Persian-Compose.git")
        }
    }
}
