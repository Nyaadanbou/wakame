package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.PriceInstanceSerializer
import cc.mewcraft.wakame.reforge.common.PriceModifierSerializer
import cc.mewcraft.wakame.reforge.common.Reforge
import cc.mewcraft.wakame.serialization.configurate.mapperfactory.ObjectMappers
import cc.mewcraft.wakame.util.NamespacedPathCollector
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import net.kyori.adventure.key.Key
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.io.File

internal object RecyclingStationSerializer {
    private const val ROOT_DIR_NAME = "recycle"
    private const val ITEMS_DIR_NAME = "items"
    private const val STATIONS_DIR_NAME = "stations"

    private val logger: Logger = Injector.get()

    fun loadAllItems(): Map<Key, PriceInstance> {
        val itemsDirectory = Injector.get<File>(named(PLUGIN_DATA_DIR))
            .resolve(Reforge.ROOT_DIR_NAME)
            .resolve(ROOT_DIR_NAME)
            .resolve(ITEMS_DIR_NAME)

        val collector = NamespacedPathCollector(itemsDirectory, true)
        val itemFiles = collector.collect("yml")

        val yamlLoader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                registerAnnotatedObjects(ObjectMappers.DEFAULT)
                kregister(PriceInstanceSerializer)
                kregister(PriceModifierSerializer)
            }
        }

        val result = itemFiles.mapNotNull { (f, ns, ps) ->
            val itemKey = Key.key(ns, ps)
            val rootNode = yamlLoader.buildAndLoadString(f.readText())

            // 反序列化配置文件
            val priceInstance = rootNode.get<PriceInstance>() ?: run {
                logger.warn("Failed to load price instance for item: $itemKey")
                return@mapNotNull null
            }

            // 构建映射
            itemKey to priceInstance
        }.toMap()

        return result
    }

    fun loadAllStations(): Map<String, RecyclingStation> {
        val tablesDirectory = Injector.get<File>(named(PLUGIN_DATA_DIR))
            .resolve(Reforge.ROOT_DIR_NAME)
            .resolve(ROOT_DIR_NAME)
            .resolve(STATIONS_DIR_NAME)

        val yamlLoader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                registerAnnotatedObjects(objectMapperFactory())
            }
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

                logger.info("Loaded recycling table: $tableId")

                tableId to table
            }

        return result
    }
}