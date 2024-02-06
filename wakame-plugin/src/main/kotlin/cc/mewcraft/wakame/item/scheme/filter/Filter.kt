package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.binary.WakaItemStack
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.WakaItem
import me.lucko.helper.random.RandomSelector

/**
 * Represents a condition testing whether a sample should be included upon
 * constructing a [RandomSelector]. For example, if the filter of `X` is
 * not met, then `X` will even not be present in the [RandomSelector].
 *
 * We primarily use it to implement the "all or nothing" feature,
 * where a type of [Core] may be present, **or not at all**, in the
 * [WakaItemStack]s created from a [WakaItem]. Compared to other public
 * projects (by 16 Jan, 2024), an "attribute" in the config must be
 * present in all instance items, where the only difference is values.
 */
sealed interface Filter : Condition<SchemeGenerationContext> {
    /**
     * Should this filter invert its original output?
     */
    val invert: Boolean

    /**
     * The semantics of the returned `boolean` is **implementation-defined**.
     */
    fun test0(context: SchemeGenerationContext): Boolean

    /**
     * Returns the test result after [invert] is applied.
     */
    override fun test(context: SchemeGenerationContext): Boolean {
        // truth table
        // col_1: fun test0()
        // col_2: val invert
        // col_3: output
        // t, t -> f
        // t, f -> t
        // f, t -> t
        // f, f -> f
        // it's exactly a XOR operation
        return test0(context) xor invert
    }
}

/**
 * Gets an empty filter.
 */
fun emptyFilter(): Filter =
    EmptyFilter
