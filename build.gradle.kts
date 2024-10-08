import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "math-research"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

repositories {
    mavenCentral()
}

val kotestVersion = "5.9.1"

dependencies {
    implementation("org.web3j:core:4.9.7")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.mockk:mockk:1.13.11")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
