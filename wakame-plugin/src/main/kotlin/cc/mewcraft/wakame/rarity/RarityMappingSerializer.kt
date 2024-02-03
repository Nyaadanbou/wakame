package cc.mewcraft.wakame.rarity

import cc.mewcraft.spatula.utils.RangeParser
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.require
import com.google.common.collect.ImmutableRangeMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal class RarityMappingSerializer : TypeSerializer<RarityMappings> {
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
     *       common: 8
     *       uncommon: 6
     *       ...
     *   tier_N:
     *     ...
     * ```
     */
    override fun deserialize(type: Type, node: ConfigurationNode): RarityMappings {
        val rangeMapBuilder = ImmutableRangeMap.builder<Int, RarityMapping>()
        node.childrenList().forEach { n1 ->
            val levelNode = n1.node("level").require<String>()
            val weightNode = n1.node("weight").takeIf { it.isMap }
                ?: throw SerializationException("`weight` node must not be virtual")

            val levelRange = RangeParser.parseIntRange(levelNode)
            val rarityMapping = RarityMapping.build {
                weightNode.childrenMap().forEach { (k, n2) ->
                    val rarityName = k.toString()
                    val rarityWeight = n2.require<Double>()
                    weight[RarityRegistry.getOrThrow(rarityName)] = rarityWeight
                }
            }

            rangeMapBuilder.put(levelRange, rarityMapping)
        }

        return RarityMappings(rangeMapBuilder.build())
    }

    override fun serialize(type: Type, obj: RarityMappings?, node: ConfigurationNode) {
        TODO("Not yet implemented")
    }
}
