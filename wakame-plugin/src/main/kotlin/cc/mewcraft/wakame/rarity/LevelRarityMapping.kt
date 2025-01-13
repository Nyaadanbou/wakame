package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.Identifiers
import cc.mewcraft.wakame.core.RegistryEntry
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.rarity.LevelRarityMapping.Entry.Companion.build
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
class LevelRarityMapping
internal constructor(
    /**
     * A RangeMap holding all the rarity mappings. The `map key` is the range
     * of levels to which the corresponding [Entry] applies, and the
     * `map value` is the corresponding [Entry].
     */
    private val rangeMap: RangeMap<Int, Entry>,
) {
    /**
     * Returns `true` if [rangeMap] is applicable to the input [lvl].
     *
     * @param lvl the input level
     */
    fun contains(lvl: Int): Boolean {
        return rangeMap.span().contains(lvl)
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
    fun pick(lvl: Int, random: Random): RegistryEntry<RarityType> {
        val rarityMapping = requireNotNull(rangeMap[lvl]) { "$lvl is not within the span ${rangeMap.span()}" }
        return rarityMapping.pick(random)
    }

    /**
     * Represents a single rarity mapping.
     *
     * To construct it, use the [build] function.
     */
    class Entry
    private constructor(
        /**
         * A weight map, where the `map key` is [RarityType] and `map value` is
         * corresponding weight.
         */
        private val weight: Map<RegistryEntry<RarityType>, Double>,
    ) {
        private val selector: RandomSelector<RegistryEntry<RarityType>> = RandomSelector.weighted(weight.keys) {
            checkNotNull(weight[it]) { "Rarity '$it' does not have weight" }
        }

        /**
         * Picks a random rarity from `this` mapping. You must first check if
         * `this` is applicable to your input level, otherwise you might get a
         * rarity that does not apply to your input level.
         */
        fun pick(random: Random): RegistryEntry<RarityType> {
            return selector.pick(random.asJavaRandom())
        }

        /**
         * The builder of [LevelRarityMapping.Entry].
         */
        interface Builder {
            val weight: MutableMap<RegistryEntry<RarityType>, Double>
            fun build(): Entry
        }

        private class BuilderImpl : Builder {
            override val weight: MutableMap<RegistryEntry<RarityType>, Double> = LinkedHashMap()
            override fun build(): Entry {
                return Entry(weight)
            }
        }

        companion object {
            fun build(builder: Builder.() -> Unit): Entry {
                return BuilderImpl().apply(builder).build()
            }
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
internal object LevelRarityMappingSerializer : TypeSerializer<LevelRarityMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): LevelRarityMapping {
        val rangeMapBuilder = ImmutableRangeMap.builder<Int, LevelRarityMapping.Entry>()
        for ((_, node1) in node.childrenMap()) {
            val levelNode = node1.node("level").krequire<String>()
            val weightNode = node1.node("weight").takeIf { it.isMap }
                ?: throw SerializationException("`weight` node must be a map")

            // deserialize weight for each rarity
            val levelRange = RangeParser.parseIntRange(levelNode)
            val levelMapping = build {
                for ((nodeKey2, node2) in weightNode.childrenMap()) {
                    val rarityTypeId = Identifiers.of(nodeKey2.toString())
                    val rarityType = KoishRegistries.RARITY.createEntry(rarityTypeId)
                    val rarityWeight = node2.krequire<Double>()
                    weight[rarityType] = rarityWeight
                }
            }

            rangeMapBuilder.put(levelRange, levelMapping)
        }

        return LevelRarityMapping(rangeMapBuilder.build())
    }
}
