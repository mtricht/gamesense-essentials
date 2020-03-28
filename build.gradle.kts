plugins {
    kotlin("jvm") version "1.3.70"
}

group = "dev.tricht.gamesense"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.+")
    implementation("com.squareup.retrofit2:retrofit:2.8.1")
    implementation("com.squareup.retrofit2:converter-jackson:2.8.1")
    implementation("com.github.bjoernpetersen:volctl:3.0.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}