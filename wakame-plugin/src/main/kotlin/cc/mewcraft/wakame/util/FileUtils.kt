package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

fun validateAssetsPathString(path: String, extension: String = ""): File? {
    return runCatching { validateAssetsPathStringOrThrow(path, extension) }.getOrNull()
}

fun validateAssetsPathStringOrThrow(path: String, extension: String = ""): File {
    val assetsDir: File = NEKO_PLUGIN.get(named(PLUGIN_ASSETS_DIR))
    val file = assetsDir.resolve(path)
    if (!file.exists())
        throw IllegalArgumentException("No such file: $file")
    if (extension.isNotEmpty() && file.extension != extension)
        throw IllegalArgumentException("Invalid file extension: $file")
    return file
}