import org.gradle.accessors.dm.LibrariesForLocal

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("com.diffplug.spotless")
    id("com.gradleup.shadow")
}

// Expose version catalog
val local = the<LibrariesForLocal>()

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.isWarnings = false
    }
    compileTestJava {
        options.encoding = "UTF-8"
        options.isWarnings = false
    }
    compileKotlin {
        compilerOptions {
            // we rely on IDE analysis
            suppressWarnings.set(true)
            freeCompilerArgs.add("-Xjvm-default=all")
            freeCompilerArgs.add("-Xnon-local-break-continue")
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    test {
        // suppress Java agent warning
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        // allow reflection to work
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
        // use JUnit 5
        useJUnitPlatform()
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/kotlin/"))
    }
}

indra {
    checkstyle().set(local.versions.checkstyle)
    javaVersions().target(21)
}

spotless {
    kotlin {
        ktlint(local.versions.ktlint.get())
    }
    kotlinGradle {
        target("*.gradle.kts", "src/*/kotlin/**.gradle.kts")
        applyCommon()
        ktlint(local.versions.ktlint.get())
    }
    format("configs") {
        target("**/*.yml", "**/*.yaml", "**/*.json")
        targetExclude("run/**")
        applyCommon(2)
    }
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
    sourceSets {
        val main by getting {
            dependencies { // Runtime are shipped with Nyaadanbou/kotlin
                compileOnly(kotlin("stdlib"))
                compileOnly(kotlin("reflect"))
                compileOnly(local.kotlinx.coroutines.core)
                compileOnly(local.kotlinx.coroutines.debug)
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation(local.kotlinx.coroutines.core)
                implementation(local.kotlinx.coroutines.test)
                implementation(local.kotlinx.coroutines.debug)
            }
        }
    }
}
