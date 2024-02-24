package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValue
import cc.mewcraft.wakame.attribute.facade.SchemeAttributeValue
import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key

/**
 * A [SchemeCore] of an attribute.
 */
data class SchemeAttributeCore(
    override val key: Key,
    override val value: SchemeAttributeValue,
) : SchemeCore {

    /**
     * Generates a [BinaryAttributeValue] with the [value] and the given
     * [context].
     *
     * Note that the returned value entirely depends on the [value] and the
     * [context]. Even if the given [context] is the same, each call of
     * this function may return a different value due to the randomness of
     * [SchemeAttributeValue].
     *
     * @param context the generation context
     * @return a new instance
     */
    override fun generate(context: SchemeGenerationContext): BinaryCoreValue {
        return value.realize(context.itemLevel)
    }
}