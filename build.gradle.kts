
plugins {
    val kotlinVersion = "1.8.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version "1.8.10"
    application
}

group = "org.anime_game_servers.gc_patcher"
version = "1.0-SNAPSHOT"
val kotlinVersion = "1.8.21"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("app.softwork:kotlinx-serialization-csv:0.0.13")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.github.ajalt.clikt:clikt:3.5.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
tasks.jar {
    manifest.attributes["Main-Class"] = "MainKt"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
