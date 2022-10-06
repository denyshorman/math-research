plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "keccak256"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotestVersion = "5.5.0"

dependencies {
    implementation("org.web3j:core:4.8.9")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("ch.qos.logback:logback-classic:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.mockk:mockk:1.13.2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        with(kotlinOptions) {
            jvmTarget = "17"

            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlin.ExperimentalUnsignedTypes",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
                "-opt-in=kotlin.time.ExperimentalTime",
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-opt-in=kotlin.experimental.ExperimentalTypeInference"
            )
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
