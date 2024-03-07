package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

fun validatePathStringOrNull(path: String, extension: String = ""): File? {
    val assetsDir: File = NEKO_PLUGIN.get(named(PLUGIN_ASSETS_DIR))
    val file = assetsDir.resolve(path)
    if (!file.exists())
        return null
    if (extension.isNotEmpty() && file.extension != extension)
        return null
    return file
}

fun validatePathString(path: String, extension: String = ""): File {
    return requireNotNull(validatePathStringOrNull(path, extension)) { "Path $path is invalid" }
}