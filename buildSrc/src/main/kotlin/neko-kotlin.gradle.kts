plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("shaded")
        dependencies {
            exclude("META-INF/NOTICE")
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/**.kotlin_module")
        }
    }
    test {
        jvmArgs("-XX:+EnableDynamicAgentLoading") // suppress Java agent warning
        useJUnitPlatform() // use JUnit 5
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
                // Basic runtime are shipped with: https://github.com/GamerCoder215/KotlinMC
                compileOnly(kotlin("stdlib"))
                compileOnly(kotlin("reflect"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KotlinxCoroutines}")
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KotlinxCoroutines}")
            }
        }
    }
}
