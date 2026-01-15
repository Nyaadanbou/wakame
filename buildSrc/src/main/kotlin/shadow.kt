import com.github.jengelman.gradle.plugins.shadow.tasks.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask

fun ShadowJar.configureForPlatform(pkg: String, platform: ServerPlatform) {
    archiveClassifier.set("shaded")

    mergeServiceFiles()
    // Needed for mergeServiceFiles to work properly in Shadow 9+
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    dependencies {
        excludePlatformRuntime(platform)
    }

    excludeMiscellaneousFiles()
}

fun ShadowJar.relocateWithPrefix(
    pkg: String,
    action: RelocateWithPrefixDSL.() -> Unit,
) {
    RelocateWithPrefixDSL(pkg, this).apply(action)
}

class RelocateWithPrefixDSL(
    private val pkg: String,
    private val shadowJar: ShadowJar,
) {
    fun move(pattern: String, result: String) {
        shadowJar.relocate(pattern, "$pkg.shaded.$result")
    }
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

// 排除平台运行时提供的依赖
fun DependencyFilter.excludePlatformRuntime(platform: ServerPlatform) {
    exclude(dependency("com.google.code.findbugs:jsr305"))
    exclude(dependency("com.google.errorprone:error_prone_annotations"))
    exclude { it.moduleGroup == "com.google.guava" }
    exclude(dependency("com.google.j2objc:j2objc-annotations"))
    exclude(dependency("io.netty:netty-all"))
    exclude(dependency("io.netty:netty-buffer"))
    exclude(dependency("org.jspecify:jspecify"))
    exclude(dependency("org.checkerframework:checker-qual"))
    exclude(dependency("org.slf4j:slf4j-api"))

    when (platform) {
        ServerPlatform.PAPER -> {
            exclude(dependency("it.unimi.dsi:fastutil"))
        }

        ServerPlatform.VELOCITY -> {

        }
    }
}