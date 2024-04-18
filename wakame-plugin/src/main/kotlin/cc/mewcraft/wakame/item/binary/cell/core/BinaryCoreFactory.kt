package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.attribute.elementOrNull
import cc.mewcraft.wakame.item.binary.cell.core.empty.BinaryEmptyCore
import cc.mewcraft.wakame.item.binary.cell.core.noop.BinaryNoopCore
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.item.schema.cell.core.attribute.SchemaAttributeCore
import cc.mewcraft.wakame.item.schema.cell.core.empty.SchemaEmptyCore
import cc.mewcraft.wakame.item.schema.cell.core.noop.SchemaNoopCore
import cc.mewcraft.wakame.item.schema.cell.core.skill.SchemaSkillCore
import cc.mewcraft.wakame.item.schema.filter.AttributeContextHolder
import cc.mewcraft.wakame.item.schema.filter.SkillContextHolder
import cc.mewcraft.wakame.util.Key
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * A factory used to create [BinaryCore] from schema and binary sources.
 */
object BinaryCoreFactory {

    /**
     * Wraps a NBT tag as [BinaryCore].
     *
     * @param compound the compound
     * @return a new instance of [BinaryCore]
     * @throws IllegalArgumentException if the NBT is malformed
     */
    fun wrap(compound: CompoundShadowTag): BinaryCore {
        if (compound.isEmpty) {
            // There's nothing in the compound,
            // so we consider it an empty core.
            return BinaryEmptyCore()
        }

        val key = Key(compound.getString(CoreBinaryKeys.CORE_IDENTIFIER))

        val ret = when {
            key == GenericKeys.NOOP -> BinaryNoopCore()
            key == GenericKeys.EMPTY -> BinaryEmptyCore()
            key.namespace() == Namespaces.ATTRIBUTE -> BinaryAttributeCore(compound)
            key.namespace() == Namespaces.SKILL -> BinarySkillCore(compound)
            else -> throw IllegalArgumentException("Failed to parse NBT tag ${compound.asString()}")
        }

        return ret
    }

    /**
     * Reifies a [SchemaCore] with given [context].
     *
     * @param context the context
     * @param schema the schema core
     * @return a new instance of [BinaryCore]
     * @throws IllegalArgumentException
     */
    fun reify(schema: SchemaCore, context: SchemaGenerationContext): BinaryCore {
        val key = schema.key
        val ret: BinaryCore
        when (schema) {
            is SchemaNoopCore, is SchemaEmptyCore -> {
                val virtualCore = schema.reify(context)
                ret = virtualCore
            }

            is SchemaAttributeCore -> {
                val attributeCore = schema.reify(context)
                context.attributes += AttributeContextHolder(key, attributeCore.operation, attributeCore.elementOrNull)
                ret = attributeCore
            }

            is SchemaSkillCore -> {
                val skillCore = schema.reify(context)
                context.skills += SkillContextHolder(key)
                ret = skillCore
            }

            else -> throw IllegalArgumentException("Failed to generate NBT tag from $schema")
        }

        return ret
    }

}
