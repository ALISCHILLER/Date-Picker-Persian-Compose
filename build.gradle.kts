buildscript {
    val agp_version by extra("8.1.2")
    val agp_version1 by extra("8.1.3")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.1.3" apply false
    `maven-publish`
}


//subprojects {
//    apply(plugin = "maven-publish")
//    configure<PublishingExtension> {
//        repositories {
//            maven {
//                name = "PersionCalendar"
//                url = uri("https://maven.pkg.github.com/ALISCHILLER/Date-Picker-Persian-Compose")
//                credentials {
//                    username = ""
//                    password = ""
//                }
//            }
//        }
//        publications {
//
//            register<MavenPublication>("gpr") {
//                version = "0.1.6"
//
//            }
//        }
//    }
//}

