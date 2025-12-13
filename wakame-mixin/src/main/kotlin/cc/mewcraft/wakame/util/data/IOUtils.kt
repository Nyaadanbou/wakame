package cc.mewcraft.wakame.util.data

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.extension

fun Path.isYaml(): Boolean {
    val ext = this.extension.lowercase()
    return ext == "yml" || ext == "yaml"
}

fun <T> File.useZip(create: Boolean = false, run: (Path) -> T): T =
    toPath().useZip(create, run)

inline fun <T> Path.useZip(create: Boolean = false, run: (Path) -> T): T {
    val env: Map<String, Any> = if (create) mapOf("create" to true) else emptyMap()
    return FileSystems.newFileSystem(this, env).use { run(it.rootDirectories.first()) }
}