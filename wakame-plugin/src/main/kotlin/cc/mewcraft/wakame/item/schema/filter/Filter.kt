package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import me.lucko.helper.random.RandomSelector

/**
 * Represents a condition testing whether a sample should be included upon
 * constructing a [RandomSelector]. For example, if the filter of `X` is
 * not met, then `X` will even not be present in the [RandomSelector].
 *
 * We primarily use it to implement the "all or nothing" feature,
 * where a type of [Core] may be present, **or not at all**, in the
 * [NekoStack]s created from a [NekoItem]. Compared to other public
 * projects (by 16 Jan, 2024), an "attribute" in the config must be
 * present in all instance items, where the only difference is values.
 */
sealed interface Filter : Condition<SchemaGenerationContext> {
    /**
     * Should this filter invert its original output?
     */
    val invert: Boolean

    /**
     * Returns the original test result before [invert] is applied.
     *
     * The semantics of the returned `boolean` is **implementation-defined**.
     */
    fun testRaw(context: SchemaGenerationContext): Boolean

    /**
     * Returns the test result after [invert] is applied.
     */
    override fun test(context: SchemaGenerationContext): Boolean {
        // truth table
        // col_1: fun test0()
        // col_2: val invert
        // col_3: output
        // t, t -> f
        // t, f -> t
        // f, t -> t
        // f, f -> f
        // it's exactly a XOR operation
        return testRaw(context) xor invert
    }
}