plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "9.0.0-beta4"
    application
}

application {
    mainClass.set("application.telegram.TelegramAppKt")
}

group = "com.antik.arkham"
version = "1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-beta4")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "application.telegram.TelegramAppKt"
    }
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.github.skydoves:sandwich:2.0.10")
    implementation("com.github.skydoves:sandwich-retrofit:2.0.10")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.2.0")
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        java {
            srcDir("build/generated/strings")
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("all")
}

tasks.register<GenerateStringsTask>("generateStrings") {
    group = "build"
    description = "Validates string files and generates Strings.kt with string identifiers."
}

tasks.named("compileKotlin") {
    dependsOn("generateStrings")
}