package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.BootstrapContextStore
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.data.useZip
import kotlin.io.path.*

/**
 * 本单例负责提取默认的配置文件.
 *
 * 欲了解具体的行为逻辑自行查看本单例的实现, 这里不再重复.
 */
internal object ConfigExtractor {

    // 要抽取的文件, 全部位于 zip 的 /configs 之下
    private val FILES_TO_EXTRACT: List<String> = listOf(
        "ability.yml",
        "attributes.yml",
        "config.yml",
        "damage/config.yml",
        "damage/attack_characteristics_mappings.yml",
        "damage/damage_type_mappings.yml",
        "damage/null_causing_mappings.yml",
        "damage/player_adhoc_mappings.yml",
        "entities.yml",
        "items.yml",
        "levels.yml",
        "resourcepack.yml",
    )

    // 要抽取的文件夹, 全部位于 zip 的 /configs 之下
    private val DIRECTORIES_TO_EXTRACT: List<String> = listOf(
        "ability/",
        "catalog/",
        "crafting_station/",
        "element/",
        "item/",
        "item2/", // TODO item2 迁移完后移除
        "item2_proxied/",
        "kizami/",
        "random/",
        "rarity/",
        "recipe/",
        "reforging_station/",
        "renderer/",
    )

    @OptIn(ExperimentalPathApi::class)
    fun extractDefaults() {
        BootstrapContextStore.PLUGIN_JAR.useZip { zip ->
            val srcRootDir = zip.resolve("configs/")
            val dstRootDir = KoishDataPaths.CONFIGS.also { it.createDirectories() }

            // 先检查文件类型是否都正确
            // 注意这里检查的是 srcRootDir 下的文件, 所以这里其实是检查程序员有没有犯错
            require(FILES_TO_EXTRACT.all { srcRootDir.resolve(it).isRegularFile() })
            require(DIRECTORIES_TO_EXTRACT.all { srcRootDir.resolve(it).isDirectory() })

            // 对于文件, 如果目标文件不存在, 则直接从压缩包中提取; 否则什么也不做
            for (file in FILES_TO_EXTRACT) {
                val srcFile = srcRootDir.resolve(file)
                val dstFile = dstRootDir.resolve(file)
                if (!dstFile.exists()) {
                    dstFile.createParentDirectories()
                    srcFile.copyTo(dstFile, overwrite = false)
                }
            }

            // 对于文件夹, 如果目标文件夹不存在或者存在但为空, 则直接从压缩包中提取; 否则什么也不做
            for (dir in DIRECTORIES_TO_EXTRACT) {
                val srcDir = srcRootDir.resolve(dir)
                val dstDir = dstRootDir.resolve(dir)
                if (!dstDir.exists() || dstDir.listDirectoryEntries().isEmpty()) {
                    srcDir.copyToRecursively(dstDir, followLinks = false, overwrite = false)
                }
            }
        }
    }

}