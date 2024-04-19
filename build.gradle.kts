import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.9.23"
    java
    id("org.beryx.runtime") version "1.13.1"
}

group = "dev.tricht.gamesense"
version = "1.12.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("com.hynnet:jacob:1.18")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    manifest {
        attributes["Implementation-Title"] = "Gamesense Essentials"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "dev.tricht.gamesense.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

application {
    mainClass.set("dev.tricht.gamesense.MainKt")
}

// TODO: Use github actions...
runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging", "java.datatransfer", "jdk.localedata"))
    jpackage {
        imageOptions.addAll(listOf("--icon", "src/main/resources/icon.ico"))
        installerOptions.addAll(
            listOf(
                "--win-per-user-install",
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut"
            )
        )
    }
    launcher {
        jvmArgs = listOf("-Djava.locale.providers=HOST")
    }
}

tasks.jre {
    doLast {
        copy {
            from("src/main/resources")
            include("jacob-1.18-x64.dll")
            into("build/jre/bin/")
        }
    }
}
