package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.commons.collections.associateNotNull
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.NamespacedPathCollector
import cc.mewcraft.wakame.util.compileFunc
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import team.unnamed.mocha.MochaEngine
import java.io.File
import java.lang.reflect.Type

internal object RerollingTableSerializer : KoinComponent {
    const val REFORGE_DIR_NAME = "reforge"
    const val REROLLING_DIR_NAME = "reroll"

    private val logger: Logger by inject()
    private val rerollDirectory by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(REFORGE_DIR_NAME).resolve(REROLLING_DIR_NAME) }

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

        val tableMainConfigNode = yamlConfig {
            withDefaults()
            serializers { kregister(Cost) }
        }.buildAndLoadString(tableMainConfigFile.readText())

        // 反序列化好的 config.yml
        val tableMainConfigData = object {
            val identifier = tableDir.name
            val enabled = tableMainConfigNode.node("enabled").getBoolean(false)
            val title = tableMainConfigNode.node("title").krequire<Component>()
            val cost = tableMainConfigNode.node("cost").krequire<RerollingTable.Cost>()
        }

        // 反序列化好的 items/
        val itemRules = NamespacedPathCollector(tableItemsDirectory, true)
            .collect("yml")
            .associateNotNull {
                val key = Key.key(it.namespace, it.path)
                val text = it.file.readText()
                val itemRuleNode = yamlConfig {
                    withDefaults()
                    serializers { kregister(CellRule) }
                }.buildAndLoadString(text)

                val cellRules = itemRuleNode.node("cells").krequire<Map<String, RerollingTable.CellRule>>()
                val itemRule = SimpleRerollingTable.ItemRule(
                    cellRules = SimpleRerollingTable.CellRuleMap(cellRules)
                    // ...
                    // ItemRule 未来会包含更多规则
                )

                key to itemRule
            }
            .toMap(HashMap())
            .let(SimpleRerollingTable::ItemRuleMap)

        return SimpleRerollingTable(
            identifier = tableMainConfigData.identifier,
            enabled = tableMainConfigData.enabled,
            title = tableMainConfigData.title,
            cost = tableMainConfigData.cost,
            itemRules = itemRules
        )
    }

    private object Cost : TypeSerializer<RerollingTable.Cost> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.Cost {
            // 计算常量
            val base = node.node("constants", "base").double
            val rarityNumberMapping = node.node("constants", "rarity_number_mapping")
                .childrenMap()
                .mapKeys { Key.key(Namespaces.ELEMENT, it.key.toString()) }
                .mapValues { it.value.double }

            // 计算方式
            val mocha = MochaEngine.createStandard()
            val eachFunctionCode = node.node("calculations", "each").krequire<String>()
            val eachFunction = mocha.compileFunc<RerollingTable.Cost.EachFunction>(eachFunctionCode)
            val totalFunctionCode = node.node("calculations", "total").krequire<String>()
            val totalFunction = mocha.compileFunc<RerollingTable.Cost.TotalFunction>(totalFunctionCode)

            return SimpleRerollingTable.Cost(
                base,
                rarityNumberMapping,
                eachFunction,
                totalFunction
            )
        }
    }

    private object CellRule : TypeSerializer<RerollingTable.CellRule> {
        override fun deserialize(type: Type, node: ConfigurationNode): RerollingTable.CellRule {
            val cost = node.node("cost").double
            val maxReroll = node.node("max_reroll").int

            return SimpleRerollingTable.CellRule(
                cost = cost,
                maxReroll = maxReroll
            )
        }
    }
}