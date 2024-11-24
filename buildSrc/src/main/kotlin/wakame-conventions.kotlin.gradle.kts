plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

// Expose version catalog
val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        compilerOptions {
            // we rely on IDE analysis
            suppressWarnings.set(true)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("shaded")
        dependencies {
            exclude("about.html")
            exclude("META-INF/licenses/**")
            exclude("META-INF/services/**")
            exclude("META-INF/LICENSE")
            exclude("META-INF/LICENSE.txt")
            exclude("META-INF/NOTICE")
            exclude("META-INF/NOTICE.txt")
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
        }
    }
    test {
        // suppress Java agent warning
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        // use JUnit 5
        useJUnitPlatform()
    }
}

// configure java sources location
sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/kotlin/"))
    }
}


java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)

    sourceSets {
        val main by getting {
            dependencies {
                // kotlin are shipped with: https://github.com/GamerCoder215/KotlinMC
                compileOnly(kotlin("stdlib"))
                compileOnly(kotlin("reflect"))
                compileOnly(local.kotlinx.coroutines.core)
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation(local.kotlinx.coroutines.core)
            }
        }
    }
}
