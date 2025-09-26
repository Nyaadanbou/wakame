package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.rarity.LevelToRarityMapping.Entry.Companion.build
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.random.RandomSelector
import com.google.common.collect.RangeMap
import org.jetbrains.annotations.ApiStatus
import kotlin.random.Random
import kotlin.random.asJavaRandom

/**
 * Represents a group of "level -> rarity" mappings.
 */
class LevelToRarityMapping
@ApiStatus.Internal
constructor(
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
    fun pick(lvl: Int, random: Random): RegistryEntry<Rarity> {
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
         * A weight map, where the `map key` is [Rarity] and `map value` is
         * corresponding weight.
         */
        private val weight: Map<RegistryEntry<Rarity>, Double>,
    ) {
        private val selector: RandomSelector<RegistryEntry<Rarity>> = RandomSelector.weighted(weight.keys) {
            checkNotNull(weight[it]) { "Rarity '$it' does not have weight" }
        }

        /**
         * Picks a random rarity from `this` mapping. You must first check if
         * `this` is applicable to your input level, otherwise you might get a
         * rarity that does not apply to your input level.
         */
        fun pick(random: Random): RegistryEntry<Rarity> {
            return selector.pick(random.asJavaRandom())
        }

        /**
         * The builder of [LevelToRarityMapping.Entry].
         */
        interface Builder {
            val weight: MutableMap<RegistryEntry<Rarity>, Double>
            fun build(): Entry
        }

        private class BuilderImpl : Builder {
            override val weight: MutableMap<RegistryEntry<Rarity>, Double> = LinkedHashMap()
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
