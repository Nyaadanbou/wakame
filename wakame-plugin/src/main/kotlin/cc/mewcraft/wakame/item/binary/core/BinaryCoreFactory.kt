package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeData
import cc.mewcraft.wakame.attribute.facade.element
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.core.SchemaAbilityCore
import cc.mewcraft.wakame.item.schema.core.SchemaAttributeCore
import cc.mewcraft.wakame.item.schema.core.SchemaCore
import cc.mewcraft.wakame.item.schema.filter.AbilityContextHolder
import cc.mewcraft.wakame.item.schema.filter.AttributeContextHolder
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.Key
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * A factory used to create [BinaryCore] from schema and binary sources.
 */
object BinaryCoreFactory {

    /**
     * Creates an [BinaryCore] from a NBT source.
     *
     * @param compound the tag
     * @return an [BinaryCore] or [emptyBinaryCore]
     * @throws IllegalArgumentException if the NBT is malformed
     */
    fun decode(compound: CompoundShadowTag): BinaryCore {
        if (compound.isEmpty) {
            return emptyBinaryCore()
        }

        val key = Key(compound.getString(NekoTags.Cell.CORE_KEY))
        val ret = when (key.namespace()) {
            NekoNamespaces.ABILITY -> {
                BinaryAbilityCore(key)
            }

            NekoNamespaces.ATTRIBUTE -> {
                val encoder = AttributeRegistry.FACADES[key].BINARY_DATA_NBT_ENCODER
                val data = encoder.encode(compound)
                BinaryAttributeCore(key, data)
            }

            else -> throw IllegalArgumentException("Failed to parse binary tag ${compound.asString()}")
        }

        return ret
    }

    /**
     * Creates an [BinaryCore] from a schema source.
     *
     * @param context the context
     * @param schemaCore the schema core
     * @return a new instance
     * @throws IllegalArgumentException
     */
    fun generate(context: SchemaGenerationContext, schemaCore: SchemaCore): BinaryCore {
        val key = schemaCore.key
        val ret = when (schemaCore) {
            is SchemaAbilityCore -> {
                // populate context
                val contextHolder = AbilityContextHolder(key)
                context.abilities += contextHolder

                // construct core
                BinaryAbilityCore(key)
            }

            is SchemaAttributeCore -> {
                // populate context
                val attributeData = schemaCore.generate(context) as BinaryAttributeData
                val contextHolder = AttributeContextHolder(key, attributeData.operation, attributeData.element)
                context.attributes += contextHolder

                // construct core
                BinaryAttributeCore(key, attributeData)
            }
        }

        return ret
    }

}
