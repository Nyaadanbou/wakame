package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.CoreMatchRule
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainerSerializer
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleSerializer
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
import xyz.xenondevs.commons.collections.associateNotNull
import java.io.File
import java.lang.reflect.Type

/**
 * [ModdingTable] 的序列化器.
 */
internal object ModdingTableSerializer {
    private const val ROOT_DIR = "mod"

    /**
     * 从配置文件夹中加载所有的定制台.
     */
    fun loadAll(): Map<String, ModdingTable> {
        val dataDir = KoishDataPaths.CONFIGS
            .resolve(ReforgingStationConstants.DATA_DIR)
            .resolve(ROOT_DIR)
            .toFile()
        val result = dataDir
            .walk()
            .maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull { f ->
                try {
                    f.name to load(dataDir, f)
                } catch (e: Exception) {
                    LOGGER.error("Can't load modding table: '${f.relativeTo(dataDir)}'. Skipped.", e)
                    return@mapNotNull null
                }
            }
            .toMap()
        return result
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
    fun load(rootDir: File, tableDir: File): ModdingTable {
        require(tableDir.isDirectory) { "Not a directory: '$tableDir'" }

        val tableMainConfigFile = tableDir.resolve("config.yml")
        val tableItemsDirectory = tableDir.resolve("items")

        // config.yml 的配置节点
        val tableMainConfigNode = yamlLoader {
            withDefaults()
            serializers {
                register(TableCurrencyCost)
                register(RarityNumberMappingSerializer)
            }
        }.buildAndLoadString(tableMainConfigFile.readText())

        // 解析主要配置
        val id = tableDir.name
        val primaryMenuSettings = tableMainConfigNode.node("primary_menu_settings").require<BasicMenuSettings>()
        val replaceMenuSettings = tableMainConfigNode.node("replace_menu_settings").require<BasicMenuSettings>()
        val reforgeCountAddMethod = tableMainConfigNode.node("reforge_count_add_method").require<ModdingTable.ReforgeCountAddMethod>()
        val rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").require<RarityNumberMapping>()
        val currencyCost = tableMainConfigNode.node("currency_cost").require<ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction>>()

        // 解析物品规则
        val itemRuleMap = NamespacedFileTreeWalker(tableItemsDirectory, "yml", true)
            .asIterable()
            .associateNotNull { (file, namespace, path) ->
                try {
                    val itemId = Key.key(namespace, path)
                    val fileText = file.readText()
                    val itemNode = yamlLoader {
                        withDefaults()
                        serializers {
                            register<ModdingTable.CoreContainerRule>(CoreContainerRule)
                            register<ModdingTable.CurrencyCost<ModdingTable.CoreContainerTotalFunction>>(CoreContainerCurrencyCost)
                            register<CoreMatchRule>(CoreMatchRuleSerializer)
                            register<CoreMatchRuleContainer>(CoreMatchRuleContainerSerializer)
                        }
                    }.buildAndLoadString(fileText)

                    val modLimit = itemNode.node("mod_limit").getInt(0)
                    // configurate 返回的是 LinkedHashMap, 保留了顺序
                    val coreContainerRuleMapData = itemNode.node("core_container").require<Map<String, ModdingTable.CoreContainerRule>>()
                    val coreContainerRuleMap = SimpleModdingTable.CoreRuleMap(LinkedHashMap(coreContainerRuleMapData))
                    val itemRule = SimpleModdingTable.ItemRule(itemId, modLimit, coreContainerRuleMap)

                    itemId to itemRule
                } catch (e: Exception) {
                    LOGGER.error("Can't load item rule: '${file.relativeTo(rootDir)}'", e)
                    return@associateNotNull null
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

    private object TableCurrencyCost : TypeSerializer2<ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction>> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> {
            val code = node.require<String>()
            val function = SimpleModdingTable.TableTotalFunction(code)
            return SimpleModdingTable.TableCurrencyCost(function)
        }
    }

    private object CoreContainerCurrencyCost : TypeSerializer2<ModdingTable.CurrencyCost<ModdingTable.CoreContainerTotalFunction>> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CurrencyCost<ModdingTable.CoreContainerTotalFunction> {
            val code = node.require<String>()
            val function = SimpleModdingTable.CoreContainerTotalFunction(code)
            return SimpleModdingTable.CoreContainerCurrencyCost(function)
        }
    }

    private object CoreContainerRule : TypeSerializer2<ModdingTable.CoreContainerRule> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CoreContainerRule {
            val currencyCost = node.node("currency_cost").require<ModdingTable.CurrencyCost<ModdingTable.CoreContainerTotalFunction>>()
            val requireElementMatch = node.node("require_element_match").getBoolean(false)
            val permission = node.node("permission").string
            val acceptedCores = node.node("accepted_cores").require<CoreMatchRuleContainer>()
            return SimpleModdingTable.CoreContainerRule(
                currencyCost = currencyCost,
                requireElementMatch = requireElementMatch,
                permission = permission,
                acceptableCores = acceptedCores,
            )
        }
    }
}