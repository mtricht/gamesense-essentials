import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.3.70"
    java
    id("org.beryx.runtime") version "1.8.0"
}

group = "dev.tricht.gamesense"
version = "1.2.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.+")
    implementation("com.squareup.retrofit2:retrofit:2.8.1")
    implementation("com.squareup.retrofit2:converter-jackson:2.8.1")
    implementation("net.java.dev.jna:jna:4.5.0")
    implementation("net.java.dev.jna:jna-platform:4.5.0")
    implementation("com.hynnet:jacob:1.18")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
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
    mainClassName = "dev.tricht.gamesense.MainKt"
}

// export JAVA_HOME="C:\\Program Files\\AdoptOpenJDK\\jdk-14.0.1.7-hotspot\\"
runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging", "java.datatransfer", "jdk.localedata"))
    jpackage {
        imageOptions.addAll(listOf("--icon", "src/main/resources/icon.ico"))
        installerOptions.addAll(listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut"))
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
