package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.item.schema.cell.core.attribute.SchemaAttributeCore
import cc.mewcraft.wakame.item.schema.cell.core.skill.SchemaSkillCore
import cc.mewcraft.wakame.item.schema.filter.AttributeContextHolder
import cc.mewcraft.wakame.item.schema.filter.SkillContextHolder
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.Key
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * A factory used to create [BinaryCore] from schema and binary sources.
 */
object BinaryCoreFactory {

    /**
     * Creates an empty binary core.
     */
    fun empty(): BinaryCore {
        return EmptyBinaryCore
    }

    /**
     * Creates an [BinaryCore] from a NBT source.
     *
     * @param compound the tag
     * @return a non-empty [BinaryCore] or [EmptyBinaryCore]
     * @throws IllegalArgumentException if the NBT is malformed
     */
    fun decode(compound: CompoundShadowTag): BinaryCore {
        if (compound.isEmpty) {
            return empty()
        }

        val key = Key(compound.getString(CoreBinaryKeys.CORE_IDENTIFIER))
        val ret: BinaryCore
        when (key.namespace()) {
            Namespaces.SKILL -> {
                ret = BinarySkillCore(key)
            }

            Namespaces.ATTRIBUTE -> {
                val encoder = AttributeRegistry.FACADES[key].BINARY_CORE_NBT_ENCODER
                val attributeCore = encoder.encode(compound)
                ret = attributeCore
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
        val ret: BinaryCore
        when (schemaCore) {
            is SchemaAttributeCore -> {
                val attributeCore = schemaCore.generate(context)
                context.attributes += AttributeContextHolder(key, attributeCore.operation, attributeCore.elementOrNull)
                ret = attributeCore
            }

            is SchemaSkillCore -> {
                val skillCore = BinarySkillCore(key)
                context.abilities += SkillContextHolder(key)
                ret = skillCore
            }

            else -> throw IllegalArgumentException("Failed to generate binary tag ${schemaCore.key.asString()}")
        }

        return ret
    }

}
