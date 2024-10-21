package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.reforge.common.*
import cc.mewcraft.wakame.skill.TriggerVariantSerializer
import cc.mewcraft.wakame.skill.trigger.SkillTriggerSerializer
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.text.Component
import org.koin.core.component.*
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File
import java.lang.reflect.Type

internal object MergingTableSerializer : KoinComponent {
    const val REFORGE_DIR_NAME = "reforge"
    const val MERGING_DIR_NAME = "merge"

    private val logger: Logger by inject()
    private val mergeDirectory by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(REFORGE_DIR_NAME).resolve(MERGING_DIR_NAME) }

    /**
     * 从配置文件夹中加载所有的合并台.
     */
    fun loadAll(): Map<String, MergingTable> {
        val map = mergeDirectory
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull {
                try {
                    val table = load(it)
                    it.name to table
                } catch (e: Exception) {
                    logger.error("Can't load merging table: '${it.relativeTo(mergeDirectory)}'", e)
                    null
                }
            }
            .associate { it }
        return map
    }

    /**
     * 读取指定的配置文件夹, 从中构建一个 [MergingTable].
     *
     * 文件的结构必须如下:
     * ```
     * tableDir/
     * ├─ config.yml
     * ```
     */
    fun load(tableDir: File): MergingTable {
        require(tableDir.isDirectory) { "Not a directory: '$tableDir'" }

        val tableMainConfigFile = tableDir.resolve("config.yml")
        val tableMainConfigNode = yamlConfig {
            withDefaults()
            serializers {
                kregister(CurrencyCost)
                kregister(NumberMergeFunction)
                kregister(OutputLevelFunction)
                kregister(OutputPenaltyFunction)

                kregister(CoreMatchRuleSerializer)
                kregister(CoreMatchRuleContainerSerializer)
                kregister(SkillTriggerSerializer)
                kregister(TriggerVariantSerializer)

                kregister(RarityNumberMappingSerializer)
            }
        }.buildAndLoadString(tableMainConfigFile.readText())

        val tableMainConfigData = object {
            val identifier = tableDir.name
            val enabled = tableMainConfigNode.node("enabled").getBoolean(false)
            val title = tableMainConfigNode.node("title").krequire<Component>()
            val maxInputItemLevel = tableMainConfigNode.node("max_input_item_level").getInt(0)
            val maxOutputItemPenalty = tableMainConfigNode.node("max_output_item_penalty").getInt(0)
            val acceptableCoreMatcher = tableMainConfigNode.node("accepted_cores").krequire<CoreMatchRuleContainer>()
            val rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").krequire<RarityNumberMapping>()
            val currencyCost = tableMainConfigNode.node("currency_cost").krequire<MergingTable.CurrencyCost>()
            val numberMergeFunction = tableMainConfigNode.node("number_merge_function").krequire<MergingTable.NumberMergeFunction>()
            val outputLevelFunction = tableMainConfigNode.node("output_level_function").krequire<MergingTable.OutputLevelFunction>()
            val outputPenaltyFunction = tableMainConfigNode.node("output_penalty_function").krequire<MergingTable.OutputPenaltyFunction>()
        }

        return SimpleMergingTable(
            identifier = tableMainConfigData.identifier,
            enabled = tableMainConfigData.enabled,
            title = tableMainConfigData.title,
            maxInputItemLevel = tableMainConfigData.maxInputItemLevel,
            maxOutputItemPenalty = tableMainConfigData.maxOutputItemPenalty,
            acceptableCoreMatcher = tableMainConfigData.acceptableCoreMatcher,
            rarityNumberMapping = tableMainConfigData.rarityNumberMapping,
            currencyCost = tableMainConfigData.currencyCost,
            numberMergeFunction = tableMainConfigData.numberMergeFunction,
            outputLevelFunction = tableMainConfigData.outputLevelFunction,
            outputPenaltyFunction = tableMainConfigData.outputPenaltyFunction
        )
    }

    private object CurrencyCost : TypeSerializer<MergingTable.CurrencyCost> {
        override fun deserialize(type: Type, node: ConfigurationNode): MergingTable.CurrencyCost {
            val code = node.string ?: "0.0"
            val function = SimpleMergingTable.CurrencyCost.TotalFunction(code)
            return SimpleMergingTable.CurrencyCost(function)
        }
    }

    private object NumberMergeFunction : TypeSerializer<MergingTable.NumberMergeFunction> {
        override fun deserialize(type: Type, node: ConfigurationNode): MergingTable.NumberMergeFunction {
            val map = node.get<Map<MergingTable.NumberMergeFunction.Type, String>>() ?: emptyMap()
            return SimpleMergingTable.NumberMergeFunction(map)
        }
    }

    private object OutputLevelFunction : TypeSerializer<MergingTable.OutputLevelFunction> {
        override fun deserialize(type: Type, node: ConfigurationNode): MergingTable.OutputLevelFunction {
            val code = node.string ?: "0.0"
            return SimpleMergingTable.OutputLevelFunction(code)
        }
    }

    private object OutputPenaltyFunction : TypeSerializer<MergingTable.OutputPenaltyFunction> {
        override fun deserialize(type: Type, node: ConfigurationNode): MergingTable.OutputPenaltyFunction {
            val code = node.string ?: "0.0"
            return SimpleMergingTable.OutputPenaltyFunction(code)
        }
    }
}