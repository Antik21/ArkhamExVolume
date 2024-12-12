plugins {
    kotlin("jvm") version "2.0.20"
    application
}

application {
    mainClass.set("application.console.ConsoleAppKt")
}

group = "com.antik.arkham"
version = "0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
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

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    val runtimeClasspath = configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    from(runtimeClasspath)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "application.console.ConsoleAppKt"
    }
}

tasks.register<Exec>("createExe") {
    dependsOn("fatJar")

    val outputDir = layout.buildDirectory.dir("exe").get().asFile
    val jarTask = tasks.named("fatJar", Jar::class).get()
    val jarFile = jarTask.archiveFile.get().asFile
    val jdkPath = System.getenv("JAVA_HOME") ?: throw GradleException("JAVA_HOME is not set")

    doFirst {
        if (!outputDir.exists()) outputDir.mkdirs()
    }

    commandLine(
        "$jdkPath\\bin\\jpackage",
        "--type", "exe",
        "--input", jarFile.parent,
        "--dest", outputDir,
        "--name", project.name,
        "--main-jar", jarFile.name,
        "--main-class", "application.console.ConsoleAppKt"
    )
}

