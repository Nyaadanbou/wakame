package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.provider
import java.util.*

fun AttributeSupplier(builder: AttributeSupplier.Builder.() -> Unit): AttributeSupplier {
    return AttributeSupplier.Builder().apply(builder).build()
}

/**
 * Responsible to provide default attribute values.
 *
 * @property instances
 */
class AttributeSupplier internal constructor(
    private val instances: Map<Attribute, AttributeInstanceBuilder>,
) {
    val vanillaAttributes: Set<Attribute> = instances.keys.filter { it.vanilla }.toSet()

    fun getAttributeInstance(attribute: Attribute): AttributeInstanceBuilder {
        val attributeInstance = instances[attribute]
            ?: throw IllegalArgumentException("Can't find attribute '${attribute.descriptionId}'")
        return attributeInstance
    }

    fun createAttributeInstance(attribute: Attribute): AttributeInstanceBuilder? {
        return instances[attribute]
    }

    fun getValue(attribute: Attribute): Double {
        return getAttributeInstance(attribute).buildToWakame().getValue()
    }

    fun getBaseValue(attribute: Attribute): Double {
        return getAttributeInstance(attribute).buildToWakame().getBaseValue()
    }

    fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        val modifier = getAttributeInstance(attribute).buildToWakame().getModifier(uuid)
            ?: throw IllegalArgumentException("Can't find modifier '$uuid' on attribute '${attribute.descriptionId}'")
        return modifier.amount
    }

    fun hasAttribute(type: Attribute): Boolean {
        return instances.containsKey(type)
    }

    fun hasModifier(type: Attribute, uuid: UUID): Boolean {
        val attributeInstance = instances[type]
        return attributeInstance?.buildToWakame()?.getModifier(uuid) != null
    }

    class Builder {
        private val builder: MutableMap<Attribute, AttributeInstanceBuilder> = HashMap()

        private fun create(attribute: Attribute): AttributeInstanceBuilder {
            val attributeInstance = AttributeInstanceBuilder(attribute)
            builder[attribute] = attributeInstance
            return attributeInstance
        }

        fun add(attribute: Attribute): Builder {
            return add(attribute, attribute.defaultValue)
        }

        fun add(attribute: Attribute, value: Attribute.() -> Provider<Double>): Builder {
            return add(attribute, value(attribute))
        }

        fun add(attribute: Attribute, value: Double): Builder {
            return add(attribute, provider(value))
        }

        fun add(attribute: Attribute, value: Provider<Double>): Builder {
            val attributeInstance = create(attribute)
            attributeInstance.baseValue = value
            return this
        }

        fun build(): AttributeSupplier {
            return AttributeSupplier(builder)
        }
    }
}