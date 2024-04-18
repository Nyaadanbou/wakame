@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeData
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * A binary core of an attribute.
 *
 * This is a top-level interface.
 */
sealed interface BinaryAttributeCore : BinaryCore, AttributeData, AttributeComponent.Op<Operation>, AttributeModifierProvider

/* Specific types of BinaryAttributeCore */

sealed interface BinaryAttributeCoreS : BinaryAttributeCore, BinaryAttributeData.S {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
    )
}

sealed interface BinaryAttributeCoreR : BinaryAttributeCore, BinaryAttributeData.R {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper),
    )
}

sealed interface BinaryAttributeCoreSE : BinaryAttributeCore, BinaryAttributeData.SE {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
        ExaminableProperty.of("element", element),
    )
}

sealed interface BinaryAttributeCoreRE : BinaryAttributeCore, BinaryAttributeData.RE {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper),
        ExaminableProperty.of("element", element),
    )
}

/* Some useful extension functions */

val BinaryAttributeCore.element: Element
    get() = requireNotNull(elementOrNull) { "The 'element' component is not present" }
val BinaryAttributeCore.elementOrNull: Element?
    get() = (this as? AttributeComponent.Element<Element>)?.element

val BinaryAttributeCore.value: Double
    get() = requireNotNull(valueOrNull) { "The 'value' component is not present" }
val BinaryAttributeCore.valueOrNull: Double?
    get() = (this as? AttributeComponent.Single<Double>)?.value

val BinaryAttributeCore.lower: Double
    get() = requireNotNull(lowerOrNull) { "The 'lower' component is not present" }
val BinaryAttributeCore.lowerOrNull: Double?
    get() = (this as? AttributeComponent.Ranged<Double>)?.lower

val BinaryAttributeCore.upper: Double
    get() = requireNotNull(upperOrNull) { "The 'upper' component is not present" }
val BinaryAttributeCore.upperOrNull: Double?
    get() = (this as? AttributeComponent.Ranged<Double>)?.upper
