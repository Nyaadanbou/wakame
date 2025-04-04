package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.rarity2.LevelToRarityMapping
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableRangeMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException

@Init(stage = InitStage.PRE_WORLD)
@Reload
internal object LevelToRarityMappingRegistryLoader : RegistryLoader {
    private const val FILE_PATH = "levels.yml"

    @InitFun
    fun init() {
        KoishRegistries2.LEVEL_TO_RARITY_MAPPING.resetRegistry()
        consumeData(KoishRegistries2.LEVEL_TO_RARITY_MAPPING::add)
        KoishRegistries2.LEVEL_TO_RARITY_MAPPING.freeze()
    }

    @InitFun
    fun reload() {
        consumeData(KoishRegistries2.LEVEL_TO_RARITY_MAPPING::update)
    }

    private fun consumeData(action: (Identifier, LevelToRarityMapping) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
        }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.childrenMap()) {
            val entry = parseEntry(nodeKey, node)
            action(entry.first, entry.second)
        }
    }

    /**
     * ## Node structure
     *
     * ```yaml
     * <node>:
     *   tier_1:
     *     level: "[0,20)"
     *     weight:
     *       common: 10.0
     *       uncommon: 5.0
     *       ...
     *   tier_2:
     *     level: "[20,40)"
     *     weight:
     *       common: 8.0
     *       uncommon: 6.0
     *       ...
     *   tier_N:
     *     ...
     * ```
     */
    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, LevelToRarityMapping> {
        val id = Identifiers.of(nodeKey.toString())
        val rangeMapBuilder = ImmutableRangeMap.builder<Int, LevelToRarityMapping.Entry>()
        for ((_, childNode) in node.childrenMap()) {
            val levelNode = childNode.node("level").require<String>()
            val weightNode = childNode.node("weight").takeIf { it.isMap }
                ?: throw SerializationException("weight node must be a map")

            // deserialize weight for each rarity
            val levelRange = RangeParser.parseIntRange(levelNode)
            val levelMapping = LevelToRarityMapping.Entry.Companion.build {
                for ((nodeKey2, node2) in weightNode.childrenMap()) {
                    val rarityTypeId = Identifiers.of(nodeKey2.toString())
                    val rarityType = KoishRegistries2.RARITY.createEntry(rarityTypeId)
                    val rarityWeight = node2.require<Double>()
                    weight[rarityType] = rarityWeight
                }
            }

            rangeMapBuilder.put(levelRange, levelMapping)
        }
        val mapping = LevelToRarityMapping(rangeMapBuilder.build())

        return id to mapping
    }
}