import com.github.jengelman.gradle.plugins.shadow.tasks.DependencyFilter

enum class ServerPlatform {
    PAPER, VELOCITY
}

// 排除平台运行时提供的依赖
fun DependencyFilter.excludePlatformRuntime(platform: ServerPlatform) {
    exclude(dependency("com.google.code.findbugs:jsr305"))
    exclude(dependency("com.google.errorprone:error_prone_annotations"))
    exclude { it.moduleGroup == "com.google.guava" }
    exclude(dependency("com.google.j2objc:j2objc-annotations"))
    exclude(dependency("io.netty:netty-all"))
    exclude(dependency("io.netty:netty-buffer"))
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