plugins {
    application
}

val libraryVersion = providers.gradleProperty("LIBRARY_VERSION").getOrElse("1.0.0")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.example.coreconsumer.Main")
}

dependencies {
    implementation("io.github.alischiller:persian-calendar-core:$libraryVersion")
}
