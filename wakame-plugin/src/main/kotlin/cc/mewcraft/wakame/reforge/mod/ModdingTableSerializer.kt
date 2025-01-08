package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainerSerializer
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleSerializer
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
import xyz.xenondevs.commons.collections.associateNotNull
import java.io.File
import java.lang.reflect.Type

/**
 * [ModdingTable] 的序列化器.
 */
internal object ModdingTableSerializer : KoinComponent {
    private const val ROOT_DIR_NAME = "mod"

    private val LOGGER: Logger = get()
    private val MODDING_DIR by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(Reforge.ROOT_DIR_NAME).resolve(ROOT_DIR_NAME) }

    /**
     * 从配置文件夹中加载所有的定制台.
     */
    fun loadAll(): Map<String, ModdingTable> {
        val map = MODDING_DIR
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull {
                try {
                    val table = load(it)
                    it.name to table
                } catch (e: Exception) {
                    LOGGER.error("Can't load modding table: '${it.relativeTo(MODDING_DIR)}'", e)
                    null
                }
            }
            .associate { it }
        return map
    }

    /**
     * 读取指定的配置文件夹, 从中构建一个 [ModdingTable].
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
     * @param tableDir 定制台的配置文件夹
     * @return 读取的 [ModdingTable]
     * @throws IllegalArgumentException 如果 [tableDir] 不是文件夹
     */
    fun load(tableDir: File): ModdingTable {
        require(tableDir.isDirectory) { "Not a directory: '$tableDir'" }

        val tableMainConfigFile = tableDir.resolve("config.yml")
        val tableItemsDirectory = tableDir.resolve("items")

        // config.yml 的配置节点
        val tableMainConfigNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(TableCurrencyCost)
                kregister(RarityNumberMappingSerializer)
            }
        }.buildAndLoadString(tableMainConfigFile.readText())

        // 解析主要配置
        val id = tableDir.name
        val primaryMenuSettings = tableMainConfigNode.node("primary_menu_settings").krequire<BasicMenuSettings>()
        val replaceMenuSettings = tableMainConfigNode.node("replace_menu_settings").krequire<BasicMenuSettings>()
        val reforgeCountAddMethod = tableMainConfigNode.node("reforge_count_add_method").krequire<ModdingTable.ReforgeCountAddMethod>()
        val rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").krequire<RarityNumberMapping>()
        val currencyCost = tableMainConfigNode.node("currency_cost").krequire<ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction>>()

        // 解析物品规则
        val itemRuleMap = NamespacedPathCollector(tableItemsDirectory, true)
            .collect("yml")
            .associateNotNull {
                try {
                    val itemId = Key.key(it.namespace, it.path)
                    val fileText = it.file.readText()
                    val itemNode = buildYamlConfigLoader {
                        withDefaults()
                        serializers {
                            kregister(CellRule)
                            kregister(CellCurrencyCost)
                            kregister(CoreMatchRuleSerializer)
                            kregister(CoreMatchRuleContainerSerializer)
                        }
                    }.buildAndLoadString(fileText)

                    val modLimit = itemNode.node("mod_limit").getInt(0)
                    // configurate 返回的是 LinkedHashMap, 保留了顺序
                    val cellRuleMapData = itemNode.node("cells").krequire<Map<String, ModdingTable.CellRule>>()
                    val cellRuleMap = SimpleModdingTable.CellRuleMap(LinkedHashMap(cellRuleMapData))
                    val itemRule = SimpleModdingTable.ItemRule(itemId, modLimit, cellRuleMap)

                    itemId to itemRule
                } catch (e: Exception) {
                    LOGGER.error("Can't load item rule: '${it.file.relativeTo(MODDING_DIR)}'", e)
                    null
                }
            }
            .toMap(HashMap())
            .let(SimpleModdingTable::ItemRuleMap)

        return SimpleModdingTable(
            id = id,
            primaryMenuSettings = primaryMenuSettings,
            replaceMenuSettings = replaceMenuSettings,
            reforgeCountAddMethod = reforgeCountAddMethod,
            rarityNumberMapping = rarityNumberMapping,
            currencyCost = currencyCost,
            itemRuleMap = itemRuleMap,
        )
    }

    private object TableCurrencyCost : TypeSerializer<ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction>> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> {
            val code = node.krequire<String>()
            val function = SimpleModdingTable.TableTotalFunction(code)
            return SimpleModdingTable.TableCurrencyCost(function)
        }
    }

    private object CellCurrencyCost : TypeSerializer<ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction>> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> {
            val code = node.krequire<String>()
            val function = SimpleModdingTable.CellTotalFunction(code)
            return SimpleModdingTable.CellCurrencyCost(function)
        }
    }

    private object CellRule : TypeSerializer<ModdingTable.CellRule> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CellRule {
            val currencyCost = node.node("currency_cost").krequire<ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction>>()
            val requireElementMatch = node.node("require_element_match").getBoolean(false)
            val permission = node.node("permission").string
            val acceptedCores = node.node("accepted_cores").krequire<CoreMatchRuleContainer>()
            return SimpleModdingTable.CellRule(
                currencyCost = currencyCost,
                requireElementMatch = requireElementMatch,
                permission = permission,
                acceptableCores = acceptedCores,
            )
        }
    }
}