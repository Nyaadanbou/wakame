plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
    sourceSets {
        val main by getting {
            dependencies {
                compileOnly(kotlin("stdlib"))
            }
        }
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/kotlin/"))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("**/*") {
            expand(project.properties)
        }
    }

    shadowJar {
        archiveClassifier.set("shaded")

        exclude("META-INF/maven/**")

        // HSCore License
        relocate("me.hsgamer.hscore.license", "me.hsgamer.bettergui.lib.license")
        // HSCore
        relocate("me.hsgamer.hscore", "me.hsgamer.bettergui.lib.core")
        // MineLib
        relocate("io.github.projectunified.minelib", "me.hsgamer.bettergui.lib.minelib")
        // bStats
        relocate("org.bstats", "me.hsgamer.bettergui.lib.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}

repositories {
    configure()
    maven {
        name = "codeMC"
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        name = "sonatypeOssPublic"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly(local.paper)
    compileOnly(local.bettergui)
}
