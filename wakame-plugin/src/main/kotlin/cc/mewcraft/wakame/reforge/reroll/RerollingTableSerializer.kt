package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.reforge.common.RarityNumberMappingSerializer
import cc.mewcraft.wakame.reforge.common.Reforge
import cc.mewcraft.wakame.util.NamespacedPathCollector
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import java.io.File
import java.lang.reflect.Type

internal object RerollingTableSerializer : KoinComponent {
    private const val ROOT_DIR_NAME = "reroll"

    private val logger: Logger = get()
    private val rerollDirectory by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(Reforge.ROOT_DIR_NAME).resolve(ROOT_DIR_NAME) }

    /**
     * 从配置文件中加载所有的重造台.
     */
    fun loadAll(): Map<String, RerollingTable> {
        val map = rerollDirectory
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull {
                try {
                    val table = load(it)
                    it.name to table
                } catch (e: Exception) {
                    logger.error("Can't load modding table: '${it.relativeTo(rerollDirectory)}'", e)
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

        val tableMainConfigNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(TableCurrencyCostSerializer)
                kregister(RarityNumberMappingSerializer)
            }
        }.buildAndLoadString(tableMainConfigFile.readText())

        // 反序列化好的 config.yml
        val identifier = tableDir.name
        val primaryMenuSettings = tableMainConfigNode.node("primary_menu_settings").krequire<BasicMenuSettings>()
        val selectionMenuSettings = tableMainConfigNode.node("selection_menu_settings").krequire<BasicMenuSettings>()
        val rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").krequire<RarityNumberMapping>()
        val currencyCost = tableMainConfigNode.node("currency_cost").krequire<RerollingTable.TableCurrencyCost>()

        // 反序列化好的 items/
        val itemRules = NamespacedPathCollector(tableItemsDirectory, true)
            .collect("yml")
            .associate {
                val itemId = Key.key(it.namespace, it.path)
                val itemRule = run {
                    val text = it.file.readText()
                    val itemRuleNode = buildYamlConfigLoader {
                        withDefaults()
                        serializers {
                            kregister(CellRuleSerializer)
                            kregister(CellCurrencyCostSerializer)
                        }
                    }.buildAndLoadString(text)

                    val modLimit = itemRuleNode.node("mod_limit").int
                    val cellRuleMapData = itemRuleNode.node("cells").krequire<Map<String, RerollingTable.CellRule>>()
                    val cellRuleMap = LinkedHashMap(cellRuleMapData)

                    // 未来可能会包含更多规则
                    // ...

                    SimpleRerollingTable.ItemRule(
                        modLimit = modLimit,
                        cellRuleMap = SimpleRerollingTable.CellRuleMap(cellRuleMap)
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

    private object TableCurrencyCostSerializer : TypeSerializer<RerollingTable.TableCurrencyCost> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.TableCurrencyCost {
            val code = node.krequire<String>()
            return SimpleRerollingTable.TableCurrencyCost(code)
        }
    }

    private object CellCurrencyCostSerializer : TypeSerializer<RerollingTable.CellCurrencyCost> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.CellCurrencyCost {
            val code = node.krequire<String>()
            return SimpleRerollingTable.CellCurrencyCost(code)
        }
    }

    private object CellRuleSerializer : TypeSerializer<RerollingTable.CellRule> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.CellRule {
            val currencyCost = node.node("currency_cost").krequire<RerollingTable.CellCurrencyCost>()
            return SimpleRerollingTable.CellRule(currencyCost)
        }
    }
}