import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.hid4java:hid4java:0.7.0")
    implementation("org.json:json:20230618")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("io.finnhub:kotlin-client:2.0.19")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}