package cc.mewcraft.wakame.item.schema.core

import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.item.BinaryCoreData
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.key.Key

/**
 * A [SchemaCore] of an attribute.
 */
data class SchemaAttributeCore(
    override val key: Key,
    override val data: SchemaAttributeData,
) : SchemaCore {
    override fun generate(context: SchemaGenerationContext): BinaryCoreData {
        val realizer = AttributeRegistry.FACADES[key].SCHEMA_DATA_REALIZER
        val factor = context.level
        val data = realizer.realize(data, factor)
        return data
    }
}