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
            freeCompilerArgs.add("-Xcontext-parameters")
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

repositories {
    // 在这里直接声明 repository 实际上违背了我们 Nyaadanbou 项目组的 conventions
    // 即, 所有 repositories 都应该由 cc.mewcraft.libraries-repository 这个 gradle 插件提供
    // 但为了方便, 就还是直接写在这里了, 以后也都尽量写在这里, 保持项目简洁

    // Messenger: https://github.com/Hexaoxide/Messenger
    maven {
        name = "jmpRepositorySnapshots"
        url = uri("https://repo.jpenilla.xyz/snapshots")
        mavenContent {
            includeGroup("de.hexaoxi")
        }
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
