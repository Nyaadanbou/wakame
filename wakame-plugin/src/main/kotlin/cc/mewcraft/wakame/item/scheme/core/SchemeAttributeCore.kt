package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.attribute.BinaryAttributeValue
import cc.mewcraft.wakame.attribute.SchemeAttributeValue
import cc.mewcraft.wakame.attribute.AttributeFacade
import cc.mewcraft.wakame.attribute.AttributeCoreCodecRegistry
import net.kyori.adventure.key.Key

/**
 * A [SchemeCore] of an attribute.
 */
data class SchemeAttributeCore(
    override val key: Key,
    override val value: SchemeAttributeValue,
) : SchemeCore {

    /**
     * Gets a [BinaryAttributeValue] generated from the [value] and the given
     * [scalingFactor].
     *
     * Note that the returned value entirely depends on the [value] and the
     * [scalingFactor]. Even if the given [scalingFactor] is the same, each
     * call of this function may return a different value due to the fact that
     * [SchemeAttributeValue] may produce a different result on each call.
     *
     * @param scalingFactor the scaling factor, such as item levels
     * @return a new random [BinaryAttributeValue]
     */
    override fun generate(scalingFactor: Int): BinaryAttributeValue {
        val codec: AttributeFacade<BinaryAttributeValue, SchemeAttributeValue> = AttributeCoreCodecRegistry.getOrThrow(key)
        val value: BinaryAttributeValue = codec.generate(value, scalingFactor)
        return value
    }
}