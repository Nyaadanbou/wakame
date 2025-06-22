import gradle.kotlin.dsl.accessors._d5be38b3b73e8f212209bbd96c1b8841.checkstyle

plugins {
    kotlin("jvm")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("com.diffplug.spotless")
    id("com.gradleup.shadow")
}

// Expose version catalog
val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

configurations.all {
    // paperweight 会把 paper 自带的 configurate 加入到 compile/runtime classpath,
    // 然后一般的 exclude 对于 paperweight.paperDevBundle 没有任何效果.
    // 这导致了 paper 的 configurate 和我们自己的 fork 在 test 环境发生冲突.
    // 不过在服务端上直接跑没有这个问题 (因为 plugin classloader 有更高优先级).
    //
    // 使用以下代码直接移除所有的 spongepowered configurate 依赖.
    exclude("org.spongepowered", "configurate-core")
    exclude("org.spongepowered", "configurate-yaml")
    exclude("org.spongepowered", "configurate-gson")
}

configurations.runtimeClasspath {
    // 服务端已经自带 kotlin 的标准库和其他辅助库.
    // 排除掉 runtime 中 kotlin 和 kotlinx 依赖.
    exclude("org.jetbrains.kotlin")
    exclude("org.jetbrains.kotlinx")
}

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
            freeCompilerArgs.add("-Xnon-local-break-continue")
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    test {
        // suppress Java agent warning
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        // allow for reflection to work
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
        // use JUnit 5
        useJUnitPlatform()
    }
    shadowJar {
        configure()
    }
}

// configure java sources location
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
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
        ktlint(local.versions.ktlint.get())
    }
}

dependencies {
    checkstyle(local.stylecheck)
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
