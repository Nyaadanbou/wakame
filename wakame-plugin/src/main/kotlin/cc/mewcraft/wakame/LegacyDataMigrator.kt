package cc.mewcraft.wakame

import java.io.File

internal object LegacyDataMigrator {

    fun migrate() {
        // 在服务端上该函数执行时 Koin 容器还未初始化,
        // 因此这里手动指定了文件路径
        val dataFolder = KoishDataPaths.ROOT.toFile()
        val cfgFolder = KoishDataPaths.CONFIGS.toFile()

        migrate0(dataFolder, cfgFolder)
    }

    private fun migrate0(dataFolder: File, cfgFolder: File) {
        // 检查是否存在旧版配置文件
        if (!dataFolder.resolve("items.yml").exists()) return

        try {
            // 确保目标目录存在
            if (!cfgFolder.exists()) {
                cfgFolder.mkdirs()
            }

            // 定义需要迁移的文件和文件夹
            val filesToMigrate = listOf(
                "config.yml", "ability.yml", "attributes.yml", "elements.yml", "entities.yml",
                "items.yml", "levels.yml", "rarities.yml", "resourcepack.yml"
            )
            val dirsToMigrate = mapOf(
                "damage" to "damage",
                "ability" to "ability",
                "crates" to "crate",
                "items" to "item",
                "kizamiz" to "kizami",
                "random" to "random",
                "recipes" to "recipe",
                "reforge" to "reforging_station",
                "renderers" to "renderer",
                "station" to "crafting_station"
            )

            // 迁移文件
            filesToMigrate.forEach { file ->
                val source = dataFolder.resolve(file)
                val target = cfgFolder.resolve(file)
                source.copyTo(target, true)
                source.delete()
            }

            // 迁移文件夹
            dirsToMigrate.forEach { (sourceDir, targetDir) ->
                val source = dataFolder.resolve(sourceDir)
                val target = cfgFolder.resolve(targetDir)
                if (source.exists()) {
                    source.copyRecursively(target)
                    source.deleteRecursively()
                }
            }
        } catch (e: Exception) {
            // 记录错误日志或采取其他措施
            throw RuntimeException("Migration failed: ${e.message}", e)
        }
    }

}