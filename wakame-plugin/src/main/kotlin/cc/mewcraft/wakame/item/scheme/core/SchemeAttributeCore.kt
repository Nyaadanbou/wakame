package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.attribute.facade.PlainAttributeData
import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.item.BinaryCoreData
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.getOrThrow
import net.kyori.adventure.key.Key

/**
 * A [SchemeCore] of an attribute.
 */
data class SchemeAttributeCore(
    override val key: Key,
    override val value: SchemaAttributeData,
) : SchemeCore {

    /**
     * Generates a [PlainAttributeData] with the [value] and the given
     * [context].
     *
     * Note that the returned value entirely depends on the [value] and the
     * [context]. Even if the given [context] is the same, each call of
     * this function may return a different value due to the randomness of
     * [SchemaAttributeData].
     *
     * @param context the generation context
     * @return a new instance
     */
    override fun generate(context: SchemeGenerationContext): BinaryCoreData {
        val baker = AttributeRegistry.schemaDataBaker.getOrThrow(key)
        val factor = context.level
        val ret = baker.bake(value, factor)
        return ret
    }
}