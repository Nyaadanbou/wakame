package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.rarity.LevelMapping.Companion.build
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.RangeParser
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.ImmutableRangeMap
import com.google.common.collect.RangeMap
import me.lucko.helper.random.RandomSelector
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import kotlin.random.Random
import kotlin.random.asJavaRandom

/**
 * Represents a group of "level -> rarity" mappings.
 */
class LevelMappings(
    /**
     * A RangeMap holding all the rarity mappings. The `map key` is the range
     * of levels to which the corresponding [LevelMapping] applies, and the
     * `map value` is the corresponding [LevelMapping].
     */
    private val mappings: RangeMap<Int, LevelMapping>,
) {
    /**
     * Returns `true` if [mappings] is applicable to the input [lvl].
     *
     * @param lvl the input level
     */
    fun contains(lvl: Int): Boolean {
        return mappings.span().contains(lvl)
    }

    /**
     * Picks a random rarity from the set of mappings in `this`. You must first
     * check if the input [lvl] is applicable by calling [contains], otherwise
     * this function **will** throw an exception.
     *
     * @param lvl the input level
     * @return a random rarity
     * @throws IllegalArgumentException
     */
    fun pick(lvl: Int, random: Random): Rarity {
        val rarityMapping = requireNotNull(mappings[lvl]) { "$lvl is not within the span ${mappings.span()}" }
        return rarityMapping.pick(random)
    }
}

/**
 * Represents a single rarity mapping.
 *
 * To construct it, use the [build] function.
 */
class LevelMapping private constructor(
    /**
     * A weight map, where the `map key` is [Rarity] and `map value` is
     * corresponding weight.
     */
    private val weight: Map<Rarity, Double>,
) {
    private val selector: RandomSelector<Rarity> = RandomSelector.weighted(weight.keys) {
        checkNotNull(weight[it]) { "Rarity '$it' does not have weight" }
    }

    /**
     * Picks a random rarity from `this` mapping. You must first check if
     * `this` is applicable to your input level, otherwise you might get a
     * rarity that does not apply to your input level.
     */
    fun pick(random: Random): Rarity {
        return selector.pick(random.asJavaRandom())
    }

    /**
     * The builder of [LevelMapping].
     */
    interface Builder {
        val weight: MutableMap<Rarity, Double>
        fun build(): LevelMapping
    }

    private class BuilderImpl : Builder {
        override val weight: MutableMap<Rarity, Double> = LinkedHashMap()
        override fun build(): LevelMapping {
            return LevelMapping(weight)
        }
    }

    companion object {
        fun build(builder: Builder.() -> Unit): LevelMapping {
            return BuilderImpl().apply(builder).build()
        }
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
internal object LevelMappingSerializer : SchemaSerializer<LevelMappings> {
    override fun deserialize(type: Type, node: ConfigurationNode): LevelMappings {
        val rangeMapBuilder = ImmutableRangeMap.builder<Int, LevelMapping>()
        node.childrenMap().forEach { (_, n1) ->
            val levelNode = n1.node("level").krequire<String>()
            val weightNode = n1.node("weight").takeIf { it.isMap }
                ?: throw SerializationException("`weight` node must be a map")

            // deserialize weight for each rarity
            val levelRange = RangeParser.parseIntRange(levelNode)
            val levelMapping = LevelMapping.build {
                weightNode.childrenMap().forEach { (k, n2) ->
                    val rarityName = k.toString()
                    val rarityWeight = n2.krequire<Double>()
                    weight[RarityRegistry.INSTANCES[rarityName]] = rarityWeight
                }
            }

            rangeMapBuilder.put(levelRange, levelMapping)
        }

        return LevelMappings(rangeMapBuilder.build())
    }
}
