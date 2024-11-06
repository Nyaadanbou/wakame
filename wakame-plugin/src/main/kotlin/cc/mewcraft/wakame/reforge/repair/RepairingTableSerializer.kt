package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.reforge.common.*
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.io.File

internal object RepairingTableSerializer {
    private const val ROOT_DIR_NAME = "repair"
    private const val ITEMS_DIR_NAME = "items"
    private const val TABLES_DIR_NAME = "tables"

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

        val collector = NamespacedPathCollector(itemsDirectory, true)
        val itemFiles = collector.collect("yml")

        val yamlLoader = yamlConfig {
            withDefaults()
            serializers {
                registerAnnotatedObjects(objectMapperFactory())
                kregister(PriceModifierSerializer)
            }
        }

        val result = itemFiles.associate { (f, ns, ps) ->
            val itemKey = Key.key(ns, ps)
            val rootNode = yamlLoader.buildAndLoadString(f.readText())

            // 反序列化配置文件
            val base = rootNode.node("base").getDouble(.0)
            val modifiers = rootNode.node("modifiers").get<Map<String, PriceModifier>>() ?: emptyMap()

            // 构建映射
            itemKey to PriceInstance(base, modifiers)
        }

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

        val yamlLoader = yamlConfig {
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
                val tableId  = f.nameWithoutExtension.lowercase()
                val rootNode = yamlLoader.buildAndLoadString(f.readText())
                val enabled = rootNode.node("enabled").getBoolean(true)
                val title = rootNode.node("title").get<Component>(Component.empty())
                val items = rootNode.node("items").getList<Key>(emptyList()).toHashSet()

                val table = SimpleRepairingTable(
                    id = tableId,
                    enabled = enabled,
                    title = title,
                    items = items
                )

                tableId to table
            }

        return result
    }
}