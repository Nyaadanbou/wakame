package cc.mewcraft.wakame.item.schema.cell.core.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.SchemaAttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.attribute.*
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import kotlin.math.max


internal data class SchemaAttributeCoreR(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : SchemaAttributeCore, SchemaAttributeData.R {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
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

internal data class SchemaAttributeCoreRE(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: Element,
) : SchemaAttributeCore, SchemaAttributeData.RE {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
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

internal data class SchemaAttributeCoreS(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val value: RandomizedValue,
) : SchemaAttributeCore, SchemaAttributeData.S {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
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

internal data class SchemaAttributeCoreSE(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: AttributeModifier.Operation,
    override val value: RandomizedValue,
    override val element: Element,
) : SchemaAttributeCore, SchemaAttributeData.SE {
    override fun reify(context: SchemaGenerationContext): BinaryAttributeCore {
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