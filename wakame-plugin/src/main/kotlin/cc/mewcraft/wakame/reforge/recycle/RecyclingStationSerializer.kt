package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.PriceInstanceSerializer
import cc.mewcraft.wakame.reforge.common.PriceModifierSerializer
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList

internal object RecyclingStationSerializer {
    private const val ROOT_DIR = "recycle"

    private const val ITEMS_DIR = "items"
    private const val STATIONS_DIR = "stations"

    fun loadAllItems(): Map<Key, PriceInstance> {
        val itemsDirectory = KoishDataPaths.CONFIGS
            .resolve(ReforgingStationConstants.DATA_DIR)
            .resolve(ROOT_DIR)
            .resolve(ITEMS_DIR)
            .toFile()

        val yamlLoader = yamlLoader {
            withDefaults()
            serializers {
                register(PriceInstanceSerializer)
                register(PriceModifierSerializer)
            }
        }

        val itemFiles = NamespacedFileTreeWalker(itemsDirectory, "yml", true)
        val result = itemFiles
            .mapNotNull { (file, namespace, path) ->
                val itemKey = Key.key(namespace, path)
                val rootNode = yamlLoader.buildAndLoadString(file.readText())

                // 反序列化配置文件
                val priceInstance = rootNode.get<PriceInstance>() ?: run {
                    LOGGER.warn("Failed to load price instance for item: $itemKey")
                    return@mapNotNull null
                }

                // 构建映射
                itemKey to priceInstance
            }.toMap()

        return result
    }

    fun loadAllStations(): Map<String, RecyclingStation> {
        val tablesDirectory = KoishDataPaths.CONFIGS
            .resolve(ReforgingStationConstants.DATA_DIR)
            .resolve(ROOT_DIR)
            .resolve(STATIONS_DIR)
            .toFile()

        val yamlLoader = yamlLoader {
            withDefaults()
        }

        val result = tablesDirectory.walk()
            .maxDepth(1)
            .drop(1)
            .filter { it.isFile && it.extension == "yml" }
            .associate { f ->
                val tableId = f.nameWithoutExtension.lowercase()
                val rootNode = yamlLoader.buildAndLoadString(f.readText())
                val items = rootNode.node("items").getList<Key>(emptyList()).toHashSet()

                val table = SimpleRecyclingStation(
                    id = tableId,
                    items = items
                )

                LOGGER.info("Loaded recycling table: $tableId")

                tableId to table
            }

        return result
    }
}