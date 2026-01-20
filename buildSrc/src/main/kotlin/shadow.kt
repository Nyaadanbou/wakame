import com.github.jengelman.gradle.plugins.shadow.tasks.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.named
import xyz.jpenilla.gremlin.gradle.ShadowGremlin
import xyz.jpenilla.gremlin.gradle.WriteDependencySet

/**
 * @param platform The target [ServerPlatform]
 */
fun ShadowJar.configure(platform: ServerPlatform) {
    archiveClassifier.set("shaded")

    mergeServiceFiles()
    // Needed for mergeServiceFiles to work properly in Shadow 9+
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    excludeMiscellaneousFiles()

    dependencies {
        excludePlatformRuntime(platform)
    }
}

/**
 * @param prefix The base package to relocate into, e.g. "koish.libs"
 * @param action The relocation actions to perform
 */
fun Task.relocateWithPrefix(
    prefix: String,
    action: KoishRelocator.() -> Unit,
) {
    KoishRelocator(prefix, this).apply(action)
}

/**
 * @param prefix The base package to relocate into, e.g. "koish.libs"
 * @param action The relocation actions to perform
 */
fun TaskContainer.relocateWithPrefix(
    prefix: String,
    action: KoishRelocator.() -> Unit,
) {
    listOf(named<ShadowJar>("shadowJar"), named<WriteDependencySet>("writeDependencies")).forEach { task ->
        task.configure {
            relocateWithPrefix(prefix, action)
        }
    }
}

/**
 * @property prefix The base package to relocate into, e.g. "koish.libs"
 * @property task The task to configure
 */
class KoishRelocator(
    private val prefix: String,
    private val task: Task,
) {
    fun move(pkg: String) {
        ShadowGremlin.relocateWithPrefix(task, prefix, pkg)
    }
}

fun KoishRelocator.moveGremlin() {
    move("me.lucko.jarrelocator")
    move("xyz.jpenilla.gremlin")
}

fun KoishRelocator.moveConfigurate() {
    move("org.spongepowered.configurate")
}

fun KoishRelocator.moveLazyConfig() {
    move("cc.mewcraft.lazyconfig")
}

fun KoishRelocator.moveMessaging() {
    move("cc.mewcraft.messaging2")
    move("ninja.egg82.messenger")
}

fun KoishRelocator.moveFastutil() {
    move("it.unimi.dsi.fastutil")
}

// 排除一些杂项文件
fun AbstractCopyTask.excludeMiscellaneousFiles() {
    exclude("**.md")
    exclude("**.html")
    exclude("META-INF/maven/**")
    exclude("META-INF/licenses/**")
    exclude("META-INF/versions/**")
    exclude("**/INFO_BIN")
    exclude("**/INFO_SRC")
    exclude("**/LICENSE**")
    exclude("**/NOTICE**")
    exclude("**/README**")
}

fun Configuration.excludePlatformConfigurate() {
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

// 排除平台运行时提供的依赖
fun Configuration.excludePlatformRuntime(platform: ServerPlatform) {
    exclude("com.google.code.findbugs", "jsr305")
    exclude("com.google.code.gson", "gson")
    exclude("com.google.errorprone", "error_prone_annotations")
    exclude("com.google.guava")
    exclude("com.google.j2objc", "j2objc-annotations")
    exclude("com.mojang")
    exclude("io.leangen.geantyref", "geantyref")
    exclude("io.netty")
    exclude("net.kyori", "adventure-api")
    exclude("net.kyori", "adventure-key")
    exclude("net.kyori", "examination-api")
    exclude("net.kyori", "examination-string")
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
    exclude("org.jetbrains.kotlin")
    exclude("org.jetbrains.kotlinx")
    exclude("org.jspecify", "jspecify")
    exclude("org.slf4j", "slf4j-api")

    when (platform) {
        ServerPlatform.PAPER -> {
            exclude("it.unimi.dsi", "fastutil")
        }

        ServerPlatform.VELOCITY -> {

        }
    }
}

// 排除平台运行时提供的依赖
fun DependencyFilter.excludePlatformRuntime(platform: ServerPlatform) {
    exclude(dependency("com.google.code.gson:gson"))
    exclude(dependency("com.google.code.findbugs:jsr305"))
    exclude(dependency("com.google.errorprone:error_prone_annotations"))
    exclude { it.moduleGroup == "com.google.guava" }
    exclude(dependency("com.google.j2objc:j2objc-annotations"))
    exclude(dependency("io.netty:netty-all"))
    exclude(dependency("io.netty:netty-buffer"))
    exclude(dependency("io.netty:netty-codec"))
    exclude(dependency("io.netty:netty-transport"))
    exclude(dependency("net.kyori:adventure-api"))
    exclude(dependency("net.kyori:adventure-bom"))
    exclude(dependency("net.kyori:adventure-text-logger-slf4j"))
    exclude(dependency("net.kyori:adventure-text-minimessage"))
    exclude(dependency("net.kyori:adventure-text-serializer-gson"))
    exclude(dependency("net.kyori:adventure-text-serializer-legacy"))
    exclude(dependency("net.kyori:adventure-text-serializer-plain"))
    exclude(dependency("org.checkerframework:checker-qual"))
    exclude { it.moduleGroup == "org.jetbrains.kotlin" }
    exclude { it.moduleGroup == "org.jetbrains.kotlinx" }
    exclude(dependency("org.jspecify:jspecify"))
    exclude(dependency("org.slf4j:slf4j-api"))

    when (platform) {
        ServerPlatform.PAPER -> {
            exclude(dependency("it.unimi.dsi:fastutil"))
        }

        ServerPlatform.VELOCITY -> {

        }
    }
}