package cc.mewcraft.wakame.rarity

import cc.mewcraft.spatula.utils.RangeParser
import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.ImmutableRangeMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

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
internal class RarityMappingsSerializer : SchemeSerializer<RarityMappings> {
    override fun deserialize(type: Type, node: ConfigurationNode): RarityMappings {
        val rangeMapBuilder = ImmutableRangeMap.builder<Int, RarityMapping>()
        node.childrenList().forEach { n1 ->
            val levelNode = n1.node("level").requireKt<String>()
            val weightNode = n1.node("weight").takeIf { it.isMap }
                ?: throw SerializationException("`weight` node must be a map")

            val levelRange = RangeParser.parseIntRange(levelNode)
            val rarityMapping = RarityMapping.build {
                weightNode.childrenMap().forEach { (k, n2) ->
                    val rarityName = k.toString()
                    val rarityWeight = n2.requireKt<Double>()
                    weight[RarityRegistry.getOrThrow(rarityName)] = rarityWeight
                }
            }

            rangeMapBuilder.put(levelRange, rarityMapping)
        }

        return RarityMappings(rangeMapBuilder.build())
    }
}
