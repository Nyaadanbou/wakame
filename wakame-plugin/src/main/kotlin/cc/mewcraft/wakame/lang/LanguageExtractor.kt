package cc.mewcraft.wakame.lang

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.data.useZip
import kotlin.io.path.*

internal object LanguageExtractor {

    fun extractDefaults() {
        BootstrapContexts.PLUGIN_JAR.useZip { zip ->
            val srcRootDir = zip.resolve("lang/")
            val dstRootDir = KoishDataPaths.LANG.also { it.createDirectories() }

            require(srcRootDir.isDirectory())
            require(dstRootDir.isDirectory())

            // lang 下的每一个文件对应一种语言的翻译.
            // 对于 srcRootDir 里的文件下面称之为 src,
            // 对于 dstRootDir 里的文件下面称之为 dst.
            // 我们遍历 srcRootDir 里的文件, 因此 src 将始终存在, 而 dst 不一定存在.
            //
            // 以下循环的行为:
            // 1) 如果 dst 不存在, 则将 src 复制到 dst
            // 2) 如果 dst 存在, 则什么也不做
            srcRootDir.walk().filter { it.isRegularFile() }.forEach { src ->
                val dst = dstRootDir.resolve(src.name)
                if (!dst.exists()) {
                    src.copyTo(dst, overwrite = false)
                }
            }
        }
    }

}