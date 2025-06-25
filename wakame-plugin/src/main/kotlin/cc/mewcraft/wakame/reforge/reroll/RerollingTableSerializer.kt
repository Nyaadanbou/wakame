package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.reforge.common.RarityNumberMappingSerializer
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.io.File
import java.lang.reflect.Type

internal object RerollingTableSerializer {
    private const val ROOT_DIR = "reroll"

    /**
     * 从配置文件中加载所有的重造台.
     */
    fun loadAll(): Map<String, RerollingTable> {
        val dataDir = KoishDataPaths.CONFIGS
            .resolve(ReforgingStationConstants.DATA_DIR)
            .resolve(ROOT_DIR)
            .toFile()
        val map = dataDir
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull {
                try {
                    val table = load(it)
                    it.name to table
                } catch (e: Exception) {
                    LOGGER.error("Can't load modding table: '${it.relativeTo(dataDir)}'", e)
                    null
                }
            }
            .associate { it }
        return map
    }

    /**
     * 读取指定的配置文件夹, 从中构建一个 [RerollingTable].
     *
     * 文件结构必须如下:
     * ```
     * tableDir/
     * ├─ config.yml
     * ├─ items/
     * │  ├─ namespace_1/
     * │  │  ├─ path_1.yml
     * │  │  ├─ path_2.yml
     * │  ├─ namespace_2/
     * │  │  ├─ path_1.yml
     * │  │  ├─ path_2.yml
     * ```
     *
     * @param tableDir 重造台的配置文件夹
     * @return 读取的 [RerollingTable]
     * @throws IllegalArgumentException 如果 [tableDir] 不符合要求
     */
    fun load(tableDir: File): RerollingTable {
        require(tableDir.isDirectory) { "Not a directory: '$tableDir'" }

        val tableMainConfigFile = tableDir.resolve("config.yml")
        val tableItemsDirectory = tableDir.resolve("items")

        val tableMainConfigNode = yamlLoader {
            withDefaults()
            serializers {
                register<RerollingTable.TableCurrencyCost>(TableCurrencyCostSerializer)
                register<RarityNumberMapping>(RarityNumberMappingSerializer)
            }
        }.buildAndLoadString(/* input = */ tableMainConfigFile.readText())

        // 反序列化好的 config.yml
        val identifier = tableDir.name
        val primaryMenuSettings = tableMainConfigNode.node("primary_menu_settings").require<BasicMenuSettings>()
        val selectionMenuSettings = tableMainConfigNode.node("selection_menu_settings").require<BasicMenuSettings>()
        val rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").require<RarityNumberMapping>()
        val currencyCost = tableMainConfigNode.node("currency_cost").require<RerollingTable.TableCurrencyCost>()

        // 反序列化好的 items/
        val itemRules = NamespacedFileTreeWalker(tableItemsDirectory, "yml", true)
            .associate {
                val itemId = Key.key(it.namespace, it.path)
                val itemRule = run {
                    val text = it.file.readText()
                    val itemRuleNode = yamlLoader {
                        withDefaults()
                        serializers {
                            register<RerollingTable.CoreContainerRule>(CoreContainerRuleSerializer)
                            register<RerollingTable.CoreContainerCurrencyCost>(CoreContainerCurrencyCostSerializer)
                        }
                    }.buildAndLoadString(text)

                    val modLimit = itemRuleNode.node("mod_limit").int
                    val coreContainerRuleMapData = itemRuleNode.node("core_container").require<Map<String, RerollingTable.CoreContainerRule>>()
                    val coreContainerRuleMap = LinkedHashMap(coreContainerRuleMapData)

                    // 未来可能会包含更多规则
                    // ...

                    SimpleRerollingTable.ItemRule(
                        modLimit = modLimit,
                        coreContainerRuleMap = SimpleRerollingTable.CoreContainerRuleMap(coreContainerRuleMap)
                    )
                }

                itemId to itemRule
            }
            .toMap(HashMap())
            .let(SimpleRerollingTable::ItemRuleMap)

        return SimpleRerollingTable(
            id = identifier,
            primaryMenuSettings = primaryMenuSettings,
            selectionMenuSettings = selectionMenuSettings,
            rarityNumberMapping = rarityNumberMapping,
            currencyCost = currencyCost,
            itemRuleMap = itemRules
        )
    }

    private object TableCurrencyCostSerializer : TypeSerializer2<RerollingTable.TableCurrencyCost> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.TableCurrencyCost {
            val code = node.require<String>()
            return SimpleRerollingTable.TableCurrencyCost(code)
        }
    }

    private object CoreContainerCurrencyCostSerializer : TypeSerializer2<RerollingTable.CoreContainerCurrencyCost> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.CoreContainerCurrencyCost {
            val code = node.require<String>()
            return SimpleRerollingTable.CoreContainerCurrencyCost(code)
        }
    }

    private object CoreContainerRuleSerializer : TypeSerializer2<RerollingTable.CoreContainerRule> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.CoreContainerRule {
            val currencyCost = node.node("currency_cost").require<RerollingTable.CoreContainerCurrencyCost>()
            return SimpleRerollingTable.CoreContainerRule(currencyCost)
        }
    }
}