package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.PriceInstanceSerializer
import cc.mewcraft.wakame.reforge.common.PriceModifierSerializer
import cc.mewcraft.wakame.reforge.common.Reforge
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import net.kyori.adventure.key.Key
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import java.io.File

internal object RepairingTableSerializer {
    private const val ROOT_DIR_NAME = "repair"
    private const val ITEMS_DIR_NAME = "items"
    private const val TABLES_DIR_NAME = "tables"

    private val logger: Logger = Injector.get()

    /**
     * 从文件中加载所有物品的 [PriceInstance].
     *
     * 该映射的键 ([Key]) 的意义:
     * - 如果是原版物品, 则为原版物品的命名空间路径, 例如: `minecraft:stone`.
     * - 如果是自定义物品, 则为自定义物品的命名空间路径, 例如: `material:tin_ingot`.
     */
    fun loadAllItems(): Map<Key, PriceInstance> {
        val itemsDirectory = Injector.get<File>(named(PLUGIN_DATA_DIR))
            .resolve(Reforge.ROOT_DIR_NAME)
            .resolve(ROOT_DIR_NAME)
            .resolve(ITEMS_DIR_NAME)

        val yamlLoader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(PriceInstanceSerializer)
                kregister(PriceModifierSerializer)
            }
        }

        val itemFiles = NamespacedFileTreeWalker(itemsDirectory, "yml", true)
        val result = itemFiles.mapNotNull { (file, namespace, path) ->
            val itemKey = Key.key(namespace, path)
            val rootNode = yamlLoader.buildAndLoadString(file.readText())

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

    /**
     * 从文件中加载所有的 [RepairingTable].
     *
     * 该映射的键 ([String]) 为 [RepairingTable.id].
     */
    fun loadAllTables(): Map<String, RepairingTable> {
        val tablesDirectory = Injector.get<File>(named(PLUGIN_DATA_DIR))
            .resolve(Reforge.ROOT_DIR_NAME)
            .resolve(ROOT_DIR_NAME)
            .resolve(TABLES_DIR_NAME)

        val yamlLoader = buildYamlConfigLoader {
            withDefaults()
        }

        val result = tablesDirectory
            .walk()
            .maxDepth(1)
            .drop(1)
            .filter { it.isFile && it.extension == "yml" }
            .associate { f ->
                val tableId = f.nameWithoutExtension.lowercase()
                val rootNode = yamlLoader.buildAndLoadString(f.readText())
                val items = rootNode.node("items").getList<Key>(emptyList()).toHashSet()

                val table = SimpleRepairingTable(
                    id = tableId,
                    items = items
                )

                logger.info("Loaded repairing table: $tableId")

                tableId to table
            }

        return result
    }
}