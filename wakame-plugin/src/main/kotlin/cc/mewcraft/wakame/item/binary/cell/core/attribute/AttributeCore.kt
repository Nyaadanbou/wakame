@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.*
import cc.mewcraft.wakame.display.FullKey
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import java.util.stream.Stream

/**
 * A binary core of an attribute.
 *
 * This is the base class of binary attribute core.
 */
sealed class BinaryAttributeCore : BinaryCore, AttributeComponent.Op<Operation>, AttributeModifierProvider {
    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].attributeModifierCreator(uuid, this)
    }

    override fun provideDisplayLore(): LoreLine {
        val lineKey = AttributeSupport.getLineKey(this)
        val lineText = AttributeRegistry.FACADES[key].displayTextCreator(this)
        return AttributeLoreLine(lineKey, lineText)
    }

    override fun toString(): String = toSimpleString()
}

/* Specific types of BinaryAttributeCore */

sealed class BinaryAttributeCoreS : BinaryAttributeCore(), AttributeDataS<Operation, Double> {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
    )
}

sealed class BinaryAttributeCoreR : BinaryAttributeCore(), AttributeDataR<Operation, Double> {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper),
    )
}

sealed class BinaryAttributeCoreSE : BinaryAttributeCore(), AttributeDataSE<Operation, Double, Element> {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
        ExaminableProperty.of("element", element),
    )
}

sealed class BinaryAttributeCoreRE : BinaryAttributeCore(), AttributeDataRE<Operation, Double, Element> {
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

private object AttributeSupport : KoinComponent {
    private val DISPLAY_KEY_FACTORY: AttributeLineKeyFactory by inject()

    fun getLineKey(core: BinaryAttributeCore): FullKey {
        return DISPLAY_KEY_FACTORY.get(core)
    }
}