package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValue
import cc.mewcraft.wakame.attribute.facade.SchemeAttributeValue
import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.getOrThrow
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
     * [context].
     *
     * Note that the returned value entirely depends on the [value] and
     * the [context]. Even if the given [context] is the same, each call
     * of this function may return a different value due to the fact that
     * [SchemeAttributeValue] may produce a different result on each call.
     *
     * @param context context
     * @return a new instance
     */
    override fun generate(context: SchemeGenerationContext): BinaryCoreValue {
        val baker = @OptIn(InternalApi::class) AttributeRegistry.schemeBakerRegistry.getOrThrow(key)
        val value = baker.bake(value, context.itemLevel) as BinaryAttributeValue
        return value
    }
}