package cc.mewcraft.wakame.feature

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.data.useZip
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

internal object FeatureExtractor {

    @OptIn(ExperimentalPathApi::class)
    fun extractDefaults() {
        BootstrapContexts.PLUGIN_JAR.useZip { zip ->
            val src = zip.resolve("features/")
            val dst = KoishDataPaths.FEATURES.also { it.createDirectories() }

            require(src.isDirectory())
            require(dst.isDirectory())

            if (!dst.exists()) {
                src.copyToRecursively(dst, followLinks = false, overwrite = false)
            }
        }
    }
}