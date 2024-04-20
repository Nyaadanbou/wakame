plugins {
    `java-library`
    id("neko.repositories") version "1.1.1-SNAPSHOT"
    id("net.kyori.indra")
    id("com.github.johnrengelman.shadow")
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    compileOnly(project(":wakame-common"))
    compileOnly(libs.server.purpur)
    compileOnly(libs.checker.qual)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("shaded")
        dependencies {
            exclude("META-INF/NOTICE")
            exclude("META-INF/LICENSE")
            exclude("META-INF/DEPENDENCIES")
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/**.kotlin_module")
        }
    }
}

java {
    withSourcesJar()
}

indra {
    javaVersions().target(21)
}