package cc.mewcraft.wakame.item.schema.cell.core.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import kotlin.math.max

/**
 * Implementation of [SchemaAttributeData.R].
 */
data class SchemaAttributeCoreR(
    override val key: Key,
    override val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : SchemaAttributeCore, SchemaAttributeData.R {
    override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
        val factor = context.level
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return BinaryAttributeCore.R(key, tagType, operation, lower, max(lower, upper))
    }
}

/**
 * Implementation of [SchemaAttributeData.RE].
 */
data class SchemaAttributeCoreRE(
    override val key: Key,
    override val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: Element,
) : SchemaAttributeCore, SchemaAttributeData.RE {
    override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
        val factor = context.level
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return BinaryAttributeCore.RE(key, tagType, operation, lower, max(lower, upper), element)
    }
}

/**
 * Implementation of [SchemaAttributeData.S].
 */
data class SchemaAttributeCoreS(
    override val key: Key,
    override val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val value: RandomizedValue,
) : SchemaAttributeCore, SchemaAttributeData.S {
    override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
        val factor = context.level
        val value = value.calculate(factor)
        return BinaryAttributeCore.S(key, tagType, operation, value)
    }
}

/**
 * Implementation of [SchemaAttributeData.SE].
 */
data class SchemaAttributeCoreSE(
    override val key: Key,
    override val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val value: RandomizedValue,
    override val element: Element,
) : SchemaAttributeCore, SchemaAttributeData.SE {
    override fun generate(context: SchemaGenerationContext): BinaryAttributeCore {
        val factor = context.level
        val value = value.calculate(factor)
        return BinaryAttributeCore.SE(key, tagType, operation, value, element)
    }
}