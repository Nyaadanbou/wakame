package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.rarity.LevelMapping.Companion.build
import com.google.common.collect.RangeMap
import me.lucko.helper.random.RandomSelector
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
