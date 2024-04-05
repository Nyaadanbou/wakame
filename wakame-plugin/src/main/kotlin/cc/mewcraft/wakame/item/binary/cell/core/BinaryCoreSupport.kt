@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeData
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeData
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.*
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import java.lang.invoke.MethodHandle
import java.util.EnumMap
import java.util.UUID

/**
 * By design, an empty [BinaryCore] is a special core in which the player
 * can replace it with something else. See the "reforge" module for more
 * details.
 */
data object EmptyBinaryCore : BinaryCore {
    override val key: Nothing get() = throw UnsupportedOperationException("EmptyBinaryCore has no key")
    override fun asShadowTag(): ShadowTag = CompoundShadowTag.create()
}

//<editor-fold desc="Binary Attribute Core">
val BinaryAttributeCore.element: Element
    get() = requireNotNull(elementOrNull) { "The 'element' component is not present" }
val BinaryAttributeCore.elementOrNull: Element?
    get() = (this as? AttributeComponent.Element<Element>)?.element
// val BinaryAttributeCore.valueOrNull: RandomizedValue?
//     get() = (this as? AttributeComponent.Single<RandomizedValue>)?.value
// val BinaryAttributeCore.lowerOrNull: Double?
//     get() = (this as? AttributeComponent.Ranged<Double>)?.lower
// val BinaryAttributeCore.upperOrNull: Double?
//     get() = (this as? AttributeComponent.Ranged<Double>)?.upper

/**
 * A binary core of an attribute.
 *
 * @property key the key of the attribute
 */
interface BinaryAttributeCore : BinaryCore, AttributeData, AttributeComponent.Op<Operation>, AttributeModifierProvider {
    /**
     * The numeric value's tag type.
     */
    val tagType: ShadowTagType

    /**
     * Makes attribute modifiers for given UUID.
     *
     * The makeAttributeModifiers function is the same for all subtypes.
     *
     * @param uuid the UUID of the attribute modifier provider
     * @return a new map of attribute modifiers
     */
    override fun makeAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    /**
     * Implementation of [BinaryAttributeData.S].
     */
    data class S(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val value: Double,
    ) : BinaryAttributeCore, BinaryAttributeData.S {
        override fun asShadowTag(): ShadowTag = CompoundShadowTag {
            putId(key)
            putNumber(NekoTags.Attribute.VAL, value, tagType)
            putOperation(operation)
        }
    }

    /**
     * Implementation of [BinaryAttributeData.R].
     */
    data class R(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val lower: Double,
        override val upper: Double,
    ) : BinaryAttributeCore, BinaryAttributeData.R {
        override fun asShadowTag(): ShadowTag = CompoundShadowTag {
            putId(key)
            putNumber(NekoTags.Attribute.MIN, lower, tagType)
            putNumber(NekoTags.Attribute.MAX, upper, tagType)
            putOperation(operation)
        }
    }

    /**
     * Implementation of [BinaryAttributeData.SE].
     */
    data class SE(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val value: Double,
        override val element: Element,
    ) : BinaryAttributeCore, BinaryAttributeData.SE {
        override fun asShadowTag(): ShadowTag = CompoundShadowTag {
            putId(key)
            putNumber(NekoTags.Attribute.VAL, value, tagType)
            putElement(element)
            putOperation(operation)
        }
    }

    /**
     * Implementation of [BinaryAttributeData.RE].
     */
    data class RE(
        override val key: Key,
        override val tagType: ShadowTagType,
        override val operation: Operation,
        override val lower: Double,
        override val upper: Double,
        override val element: Element,
    ) : BinaryAttributeCore, BinaryAttributeData.RE {
        override fun asShadowTag(): ShadowTag = CompoundShadowTag {
            putId(key)
            putNumber(NekoTags.Attribute.MIN, lower, tagType)
            putNumber(NekoTags.Attribute.MAX, upper, tagType)
            putElement(element)
            putOperation(operation)
        }
    }
}

/* Specialized Compound Operations */

private fun CompoundShadowTag.putElement(element: Element) {
    this.putByte(NekoTags.Attribute.ELEMENT, element.binaryId)
}

private fun CompoundShadowTag.putOperation(operation: Operation) {
    this.putByte(NekoTags.Attribute.OPERATION, operation.binary)
}

// Map Value's KFunction Type: CompoundShadowTag.(String, Number) -> Unit
private val TAG_TYPE_2_TAG_SETTER_MAP: Map<ShadowTagType, MethodHandle> =
    buildMap {
        this[ShadowTagType.BYTE] = CompoundShadowTag::putByte
        this[ShadowTagType.SHORT] = CompoundShadowTag::putShort
        this[ShadowTagType.INT] = CompoundShadowTag::putInt
        this[ShadowTagType.LONG] = CompoundShadowTag::putLong
        this[ShadowTagType.FLOAT] = CompoundShadowTag::putFloat
        this[ShadowTagType.DOUBLE] = CompoundShadowTag::putDouble
    }.mapValues {
        it.value.toMethodHandle()
    }.let {
        EnumMap(it)
    }

// Map Value's KFunction Type: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<ShadowTagType, MethodHandle> =
    buildMap {
        this[ShadowTagType.BYTE] = Number::toStableByte
        this[ShadowTagType.SHORT] = Number::toStableShort
        this[ShadowTagType.INT] = Number::toStableInt
        this[ShadowTagType.LONG] = Number::toStableLong
        this[ShadowTagType.FLOAT] = Number::toStableFloat
        this[ShadowTagType.DOUBLE] = Number::toStableDouble
    }.mapValues {
        it.value.toMethodHandle()
    }.let {
        EnumMap(it)
    }

private fun CompoundShadowTag.putNumber(key: String, value: Double, shadowTagType: ShadowTagType) {
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getValue(shadowTagType).invoke(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getValue(shadowTagType).invoke(this, key, converted)
}

private fun CompoundShadowTag.putId(id: Key) {
    this.putString(NekoTags.Cell.CORE_KEY, id.asString())
}
//</editor-fold>

//<editor-fold desc="Binary Skill Core">
/**
 * A binary core of a skill.
 *
 * @property key the key of the skill
 */
data class BinarySkillCore(
    override val key: Key,
) : BinaryCore {
    override fun asShadowTag(): ShadowTag = CompoundShadowTag {
        putString(NekoTags.Cell.CORE_KEY, key.asString())
    }
}
//</editor-fold>