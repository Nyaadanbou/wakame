package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainerSerializer
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleSerializer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.reforge.common.RarityNumberMappingSerializer
import cc.mewcraft.wakame.reforge.common.Reforge
import cc.mewcraft.wakame.serialization.configurate.mapperfactory.ObjectMappers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

internal object MergingTableSerializer : KoinComponent {
    private const val ROOT_DIR_NAME = "merge"

    private val MERGE_DIRECTORY by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(Reforge.ROOT_DIR_NAME).resolve(ROOT_DIR_NAME) }

    /**
     * 从配置文件夹中加载所有的合并台.
     */
    fun loadAll(): Map<String, MergingTable> {
        val map = MERGE_DIRECTORY
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull { f ->
                try {
                    val table = load(f)
                    val name = f.name
                    name to table
                } catch (e: Exception) {
                    LOGGER.error("Can't load merging table: '${f.relativeTo(MERGE_DIRECTORY)}'", e)
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
        require(tableDir.isDirectory) { "not a directory: '$tableDir'" }

        val tableMainConfigFile = tableDir.resolve("config.yml")
        val tableMainConfigNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(CoreMatchRuleSerializer)
                kregister(CoreMatchRuleContainerSerializer)
                kregister(RarityNumberMappingSerializer)
                registerAnnotatedObjects(ObjectMappers.DEFAULT)
            }
        }.buildAndLoadString(tableMainConfigFile.readText())

        val tableMainConfigData = object {
            val id = tableDir.name
            val primaryMenuSettings = tableMainConfigNode.node("primary_menu_settings").krequire<BasicMenuSettings>()
            val inputLevelLimit = tableMainConfigNode.node("input_level_limit").getInt(0)
            val outputLevelLimit = tableMainConfigNode.node("output_level_limit").getInt(0)
            val outputPenaltyLimit = tableMainConfigNode.node("output_penalty_limit").getInt(0)
            val acceptableCoreMatcher = tableMainConfigNode.node("accepted_cores").krequire<CoreMatchRuleContainer>()
            val rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").krequire<RarityNumberMapping>()
            val totalCost = tableMainConfigNode.node("total_cost").krequire<SimpleMergingTable.CurrencyCost>()
            val valueMergeMethod = tableMainConfigNode.node("value_merge_method").krequire<SimpleMergingTable.ValueMergeMethod>()
            val levelMergeMethod = tableMainConfigNode.node("level_merge_method").krequire<SimpleMergingTable.LevelMergeMethod>()
            val penaltyMergeMethod = tableMainConfigNode.node("penalty_merge_method").krequire<SimpleMergingTable.PenaltyMergeMethod>()
        }

        return SimpleMergingTable(
            id = tableMainConfigData.id,
            primaryMenuSettings = tableMainConfigData.primaryMenuSettings,
            inputLevelLimit = tableMainConfigData.inputLevelLimit,
            outputLevelLimit = tableMainConfigData.outputLevelLimit,
            outputPenaltyLimit = tableMainConfigData.outputPenaltyLimit,
            acceptableCoreMatcher = tableMainConfigData.acceptableCoreMatcher,
            rarityNumberMapping = tableMainConfigData.rarityNumberMapping,
            totalCost = tableMainConfigData.totalCost,
            valueMergeMethod = tableMainConfigData.valueMergeMethod,
            levelMergeMethod = tableMainConfigData.levelMergeMethod,
            penaltyMergeMethod = tableMainConfigData.penaltyMergeMethod,
        )
    }
}