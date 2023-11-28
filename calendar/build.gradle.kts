plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    id("signing")
}

android {
    namespace = "com.msa.calendar"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }


}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
}


//publishing {
//    repositories {
//        maven {
//            name = "PersionCalendar"
//            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
//            credentials {
//                username = project.findProperty("mavenCentralUsername")?.toString() ?: System.getenv("MAVEN_USERNAME")
//                password = project.findProperty("mavenCentralPassword")?.toString() ?: System.getenv("MAVEN_PASSWORD")
//            }
//        }
//    }
//    publications {
//        register<MavenPublication>("release") {
//            groupId = "com.msa"
//            artifactId = "calendar"
//            version = "0.1.3"
//            afterEvaluate {
//                from(components["release"])
//            }
//
//            pom {
//                name.set("PersionCalendar")
//                description.set("An extensible persion calendar for the jetpack composition system.")
//                url.set("https://github.com/ALISCHILLER/Date-Picker-Persian-Compose")
//                licenses {
//                    license {
//                        name.set(" GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007")
//                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
//                        distribution.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("ALISCHILLER")
//                        name.set("ALI Soleimani")
//                        url.set("https://github.com/ALISCHILLER")
//                    }
//                }
//                scm {
//                    url.set("https://github.com/ALISCHILLER/Date-Picker-Persian-Compose")
//                    connection.set("scm:git:git://ALISCHILLER/Date-Picker-Persian-Compose.git")
//                    developerConnection.set("scm:git:ssh://git@github.com:codeandtheory/YCharts.git")
//                }
//            }
//        }
//    }
//}
//signing {
//    useInMemoryPgpKeys(
//        project.findProperty("signing.keyId")?.toString() ?: System.getenv("SIGNINGKEY"),
//        project.findProperty("signing.InMemoryKey")?.toString() ?: System.getenv("MEMORY_KEY"),
//        project.findProperty("signing.password")?.toString()?:System.getenv("SIGNINGPASSWORD")
//    )
//    sign(publishing.publications)
//}

//publishing {
//    publications {
//        register<MavenPublication>("release") {
////            groupId = "com.msa"
////            artifactId = "calendar"
//            version = "0.1.6"
////            artifact("$buildDir/outputs/aar/calendar-release.aar")
//
//            pom {
//                name.set("PersionCalendar")
//                description.set("An extensible Persian calendar for the Jetpack Compose system.")
//                url.set("https://github.com/ALISCHILLER/Date-Picker-Persian-Compose")
//                licenses {
//                    license {
//                        name.set("GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007")
//                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
//                        distribution.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("ALISCHILLER")
//                        name.set("ALI Soleimani")
//                        url.set("https://github.com/ALISCHILLER")
//                    }
//                }
//                scm {
//                    url.set("https://github.com/ALISCHILLER/Date-Picker-Persian-Compose")
//                    connection.set("scm:git:git://ALISCHILLER/Date-Picker-Persian-Compose.git")
//                    developerConnection.set("scm:git:ssh://git@github.com:codeandtheory/YCharts.git")
//                }
//            }
//        }
//    }
//
//    repositories {
//        maven {
//            name = "PersionCalendar"
//            url = uri("https://maven.pkg.github.com/ALISCHILLER/Date-Picker-Persian-Compose")
//            credentials {
//                username = ""
//                password = ""
//            }
//        }
//    }
//}






