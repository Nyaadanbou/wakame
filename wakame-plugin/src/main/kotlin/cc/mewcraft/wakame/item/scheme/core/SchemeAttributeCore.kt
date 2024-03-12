package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.item.BinaryCoreData
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.key.Key

/**
 * A [SchemeCore] of an attribute.
 */
data class SchemeAttributeCore(
    override val key: Key,
    override val data: SchemaAttributeData,
) : SchemeCore {
    override fun generate(context: SchemeGenerationContext): BinaryCoreData {
        val baker = AttributeRegistry.schemaDataBaker.getValue(key)
        val fact = context.level
        val data = baker.bake(data, fact)
        return data
    }
}