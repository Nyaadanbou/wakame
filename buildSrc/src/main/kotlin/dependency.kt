import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.koishLoader(dependencyNotation: Any) {
    add("runtimeDownload", dependencyNotation)
    add("compileOnlyApi", dependencyNotation)
}