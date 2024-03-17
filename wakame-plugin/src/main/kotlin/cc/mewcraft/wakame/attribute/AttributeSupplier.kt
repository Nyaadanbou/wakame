package cc.mewcraft.wakame.attribute

import java.util.UUID

fun AttributeSupplier(builder: AttributeSupplier.Builder.() -> Unit): AttributeSupplier {
    return AttributeSupplier.Builder().apply(builder).build()
}

/**
 * Responsible to provide default attribute values.
 *
 * @property instances
 */
class AttributeSupplier internal constructor(
    private val instances: Map<Attribute, AttributeInstance>,
) {
    fun getAttributeInstance(attribute: Attribute): AttributeInstance {
        val attributeInstance = instances[attribute]
            ?: throw IllegalArgumentException("Can't find attribute '${attribute.descriptionId}'")
        return attributeInstance
    }

    fun createAttributeInstance(attribute: Attribute): AttributeInstance? {
        val attributeInstance = instances[attribute] ?: return null
        val attributeInstance2 = WakameAttributeInstance(attribute)
        attributeInstance2.replace(attributeInstance)
        return attributeInstance2
    }

    fun getValue(attribute: Attribute): Double {
        return getAttributeInstance(attribute).getValue()
    }

    fun getBaseValue(attribute: Attribute): Double {
        return getAttributeInstance(attribute).getBaseValue()
    }

    fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        val modifier = getAttributeInstance(attribute).getModifier(uuid)
            ?: throw IllegalArgumentException("Can't find modifier '$uuid' on attribute '${attribute.descriptionId}'")
        return modifier.amount
    }

    fun hasAttribute(type: Attribute): Boolean {
        return instances.containsKey(type)
    }

    fun hasModifier(type: Attribute, uuid: UUID): Boolean {
        val attributeInstance = instances[type]
        return attributeInstance?.getModifier(uuid) != null
    }

    class Builder {
        private val builder: MutableMap<Attribute, AttributeInstance> = HashMap()

        private fun create(attribute: Attribute): AttributeInstance {
            val attributeInstance = WakameAttributeInstance(attribute)
            builder[attribute] = attributeInstance
            return attributeInstance
        }

        fun add(attribute: Attribute): Builder {
            create(attribute)
            return this
        }

        fun add(attribute: Attribute, value: Double): Builder {
            val attributeInstance = create(attribute)
            attributeInstance.setBaseValue(value)
            return this
        }

        fun build(): AttributeSupplier {
            return AttributeSupplier(builder)
        }
    }
}