package cc.mewcraft.wakame.item.schema.cell.core.attribute

import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeDataR
import cc.mewcraft.wakame.attribute.facade.AttributeDataRE
import cc.mewcraft.wakame.attribute.facade.AttributeDataS
import cc.mewcraft.wakame.attribute.facade.AttributeDataSE
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCoreDataHolderR
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCoreDataHolderRE
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCoreDataHolderS
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCoreDataHolderSE
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.filter.AttributeContextHolder
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import kotlin.math.max

private fun SchemaAttributeCore.populateGenerationContext(context: SchemaGenerationContext) {
    context.attributes += AttributeContextHolder(key, operation, elementOrNull)
}

data class SchemaAttributeCoreR(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : SchemaAttributeCore, AttributeDataR<RandomizedValue> {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
        populateGenerationContext(context)

        val factor = context.level
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return BinaryAttributeCoreDataHolderR(key, tagType, operation, lower, max(lower, upper))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper)
    )

    override fun toString(): String = toSimpleString()
}

data class SchemaAttributeCoreRE(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: Element,
) : SchemaAttributeCore, AttributeDataRE<RandomizedValue> {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
        populateGenerationContext(context)

        val factor = context.level
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return BinaryAttributeCoreDataHolderRE(key, tagType, operation, lower, max(lower, upper), element)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}

data class SchemaAttributeCoreS(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val value: RandomizedValue,
) : SchemaAttributeCore, AttributeDataS<RandomizedValue> {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
        populateGenerationContext(context)

        val factor = context.level
        val value = value.calculate(factor)
        return BinaryAttributeCoreDataHolderS(key, tagType, operation, value)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
    )

    override fun toString(): String = toSimpleString()
}

data class SchemaAttributeCoreSE(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val value: RandomizedValue,
    override val element: Element,
) : SchemaAttributeCore, AttributeDataSE<RandomizedValue> {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
        populateGenerationContext(context)

        val factor = context.level
        val value = value.calculate(factor)
        return BinaryAttributeCoreDataHolderSE(key, tagType, operation, value, element)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}