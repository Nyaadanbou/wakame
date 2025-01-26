package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.*
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import java.io.File

internal object MergingTableSerializer {
    private const val ROOT_DIR = "merge"

    /**
     * 从配置文件夹中加载所有的合并台.
     */
    fun loadAll(): Map<String, MergingTable> {
        val dataDir = Injector.get<File>(InjectionQualifier.CONFIGS_FOLDER)
            .resolve(ReforgingStationConstants.DATA_DIR)
            .resolve(ROOT_DIR)
        val map = dataDir
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull { f ->
                try {
                    val table = load(f)
                    val name = f.name
                    name to table
                } catch (e: Exception) {
                    LOGGER.error("Can't load merging table: '${f.relativeTo(dataDir)}'", e)
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
        val tableMainConfigNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<CoreMatchRule>(CoreMatchRuleSerializer)
                register<CoreMatchRuleContainer>(CoreMatchRuleContainerSerializer)
                register<RarityNumberMapping>(RarityNumberMappingSerializer)
            }
        }.buildAndLoadString(tableMainConfigFile.readText())

        return SimpleMergingTable(
            id = tableDir.name,
            primaryMenuSettings = tableMainConfigNode.node("primary_menu_settings").require<BasicMenuSettings>(),
            inputLevelLimit = tableMainConfigNode.node("input_level_limit").getInt(0),
            outputLevelLimit = tableMainConfigNode.node("output_level_limit").getInt(0),
            outputPenaltyLimit = tableMainConfigNode.node("output_penalty_limit").getInt(0),
            acceptableCoreMatcher = tableMainConfigNode.node("accepted_cores").require<CoreMatchRuleContainer>(),
            rarityNumberMapping = tableMainConfigNode.node("rarity_number_mapping").require<RarityNumberMapping>(),
            totalCost = tableMainConfigNode.node("total_cost").require<SimpleMergingTable.CurrencyCost>(),
            valueMergeMethod = tableMainConfigNode.node("value_merge_method").require<SimpleMergingTable.ValueMergeMethod>(),
            levelMergeMethod = tableMainConfigNode.node("level_merge_method").require<SimpleMergingTable.LevelMergeMethod>(),
            penaltyMergeMethod = tableMainConfigNode.node("penalty_merge_method").require<SimpleMergingTable.PenaltyMergeMethod>(),
        )
    }
}