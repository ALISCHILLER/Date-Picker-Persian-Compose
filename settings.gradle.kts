pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // jitpack فقط اگر واقعاً لازم داری:
        // maven(url = "https://jitpack.io")
    }
}

rootProject.name = "PersionCalendar"
include(":app")
include(":calendar")
