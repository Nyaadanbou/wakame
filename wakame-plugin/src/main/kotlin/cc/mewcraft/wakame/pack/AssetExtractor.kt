package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.KoishBootstrapContexts
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.data.useZip
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

internal object AssetExtractor {

    fun extractDefaults() {
        KoishBootstrapContexts.PLUGIN_JAR.useZip { zip ->
            val src = zip.resolve("assets/")
            val dst = KoishDataPaths.ASSETS.also { it.createDirectories() }

            require(src.isDirectory())
            require(dst.isDirectory())

            if (!dst.exists()) {
                src.toFile().copyRecursively(dst.toFile(), overwrite = false)
            }
        }
    }

}