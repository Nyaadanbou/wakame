plugins {
    kotlin("jvm")
    id("net.kyori.indra")
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
        // allow for reflection to work
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
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

indra {
    javaVersions().target(21)
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
                implementation(local.kotlinx.coroutines.debug)
            }
        }
    }
}
