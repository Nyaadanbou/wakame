package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.data.useZip
import kotlin.io.path.*

internal object AssetExtractor {

    @OptIn(ExperimentalPathApi::class)
    fun extractDefaults() {
        BootstrapContexts.PLUGIN_JAR.useZip { zip ->
            val src = zip.resolve("assets/")
            val dst = KoishDataPaths.ASSETS.also { it.createDirectories() }

            require(src.isDirectory())
            require(dst.isDirectory())

            if (!dst.exists()) {
                src.copyToRecursively(dst, followLinks = false, overwrite = false)
            }
        }
    }

}