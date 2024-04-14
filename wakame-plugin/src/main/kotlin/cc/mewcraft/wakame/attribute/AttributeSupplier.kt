package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
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
    private val instances: Map<Attribute, AttributeInstanceProxy>,
) {
    val attributes: Set<Attribute>
        get() = instances.keys

    fun getAttributeInstance(attribute: Attribute): AttributeInstanceProxy {
        val attributeInstance = instances[attribute]
            ?: throw IllegalArgumentException("Can't find attribute '${attribute.descriptionId}'")
        return attributeInstance
    }

    fun createAttributeInstance(attribute: Attribute): AttributeInstanceProxy? {
        val attributeInstance = instances[attribute] ?: return null
        val attributeInstance2 = AttributeInstanceProxy(attribute)
        attributeInstance2.replace(attributeInstance)
        return attributeInstance2
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
        private val builder: MutableMap<Attribute, AttributeInstanceProxy> = HashMap()
        private var config: ConfigProvider? = null

        private fun create(attribute: Attribute): AttributeInstanceProxy {
            val attributeInstance = AttributeInstanceProxy(attribute)
            builder[attribute] = attributeInstance
            return attributeInstance
        }

        fun withConfig(config: ConfigProvider): Builder {
            this.config = config
            return this
        }

        fun add(attribute: Attribute): Builder {
            create(attribute)
            return this
        }

        fun add(attribute: Attribute, value: Double): Builder {
            return add(attribute, provider(value))
        }

        fun add(attribute: Attribute, value: Provider<Double>): Builder {
            val attributeInstance = create(attribute)
            attributeInstance.setBaseValue(value.get())
            return this
        }

        fun build(): AttributeSupplier {
            if (config != null) {
                for (instance in builder) {
                    val attribute = instance.value.attribute
                    val baseValue = if (attribute is ElementAttribute) {
                        config!!.optionalEntry<Double>(attribute.element.uniqueId, attribute.descriptionId)
                    } else {
                        config!!.optionalEntry<Double>(attribute.descriptionId)
                    }.get()
                    if (baseValue == null)
                        continue
                    instance.value.setBaseValue(baseValue)
                }
            }
            return AttributeSupplier(builder)
        }
    }
}