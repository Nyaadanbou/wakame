package cc.mewcraft.wakame.rarity

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.serializer.NamedTextColorSerializer
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.RangeParser
import cc.mewcraft.wakame.util.configurate.yamlLoader
import com.google.common.collect.ImmutableRangeMap
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException

@Init(InitStage.BOOTSTRAP)
internal object RarityRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.RARITY.resetRegistry()
        consumeRarityData(BuiltInRegistries.RARITY::add)
        BuiltInRegistries.RARITY.freeze()

        BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.resetRegistry()
        consumeMappingData(BuiltInRegistries.LEVEL_TO_RARITY_MAPPING::add)
        BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.freeze()
    }

    fun reload() {
        consumeRarityData(BuiltInRegistries.RARITY::update)
        consumeMappingData(BuiltInRegistries.LEVEL_TO_RARITY_MAPPING::update)
    }

    private fun consumeRarityData(registryAction: (Identifier, Rarity) -> Unit) {
        val rootDirectory = getFileInConfigDirectory("rarity/")
        val entryDirectory = rootDirectory.resolve("entries/")
        val loader = yamlLoader {
            withDefaults()
            serializers {
                register(NamedTextColorSerializer)
            }
        }
        entryDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val rarityId = f.relativeTo(entryDirectory).invariantSeparatorsPath.substringBeforeLast('.')
                val entry = parseRarityEntry(rarityId, rootNode)
                registryAction(entry.first, entry.second)
            } catch (e: Exception) {
                IdePauser.pauseInIde(e)
                LOGGER.error("Failed to load rarity from file: ${f.toRelativeString(rootDirectory)}")
            }
        }
    }

    private fun consumeMappingData(action: (Identifier, LevelToRarityMapping) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
        }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory("levels.yml").readText())
        for ((nodeKey, node) in rootNode.childrenMap()) {
            try {
                val entry = parseMappingEntry(nodeKey, node)
                action(entry.first, entry.second)
            } catch (e: Exception) {
                IdePauser.pauseInIde(e)
                LOGGER.error("Failed to load level to rarity mapping from node: $nodeKey")
            }
        }
    }

    /**
     * ## Node structure
     *
     * ```yaml
     * <root>:
     *   name: 史诗
     *   color: red
     *   styles: []
     *   weight: 1
     * ```
     */
    private fun parseRarityEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, Rarity> {
        val id = Identifiers.of(nodeKey.toString())
        val name = node.node("name").get<Component>(Component.text(id.asString()))
        val styles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())
        val weight = node.node("weight").get<Int>(0)
        val color = node.node("color").get<NamedTextColor>()
        val enchantSlotBase = node.node("enchant_slot_base").get<Int>(0)
        val enchantSlotLimit = node.node("enchant_slot_limit").get<Int>(Int.MIN_VALUE)
        val rarityType = Rarity(
            displayName = name,
            displayStyles = styles,
            weight = weight,
            color = color,
            enchantSlotBase = enchantSlotBase,
            enchantSlotLimit = enchantSlotLimit,
        )
        return id to rarityType
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
    private fun parseMappingEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, LevelToRarityMapping> {
        val id = Identifiers.of(nodeKey.toString())
        val rangeMapBuilder = ImmutableRangeMap.builder<Int, LevelToRarityMapping.Entry>()
        for ((_, childNode) in node.childrenMap()) {
            val levelNode = childNode.node("level").require<String>()
            val weightNode = childNode.node("weight").takeIf { it.isMap }
                ?: throw SerializationException("weight node must be a map")
            // deserialize weight for each rarity
            val levelRange = RangeParser.parseIntRange(levelNode)
            val levelMapping = LevelToRarityMapping.Entry.build {
                for ((nodeKey2, node2) in weightNode.childrenMap()) {
                    val rarityTypeId = Identifiers.of(nodeKey2.toString())
                    val rarityType = BuiltInRegistries.RARITY.createEntry(rarityTypeId)
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
