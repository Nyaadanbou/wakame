package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.registry.ATTRIBUTE_CONFIG_FILE
import org.bukkit.entity.EntityType
import java.util.*

private val ATTRIBUTE_CONFIG by lazy { Configs.YAML[ATTRIBUTE_CONFIG_FILE] }

fun AttributeSupplier(entityType: EntityType, builder: AttributeSupplier.Builder.() -> Unit): AttributeSupplier {
    return AttributeSupplier.Builder(entityType).apply(builder).build()
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

    class Builder(
        private val entityType: EntityType,
    ) {
        private val builder: MutableMap<Attribute, AttributeInstanceBuilder> = HashMap()

        private fun create(attribute: Attribute): AttributeInstanceBuilder {
            val attributeInstance = AttributeInstanceBuilder(attribute)
            builder[attribute] = attributeInstance
            return attributeInstance
        }

        /**
         * 添加一个属性，其 [AttributeInstance] 的 [AttributeInstance.getBaseValue] 的值将与 [Attribute.defaultValue] 相同。
         */
        fun add(attribute: Attribute): Builder {
            return addByStatic(attribute, attribute.defaultValue)
        }

        /**
         * 添加一个属性，其 [AttributeInstance] 的 [AttributeInstance.getBaseValue] 的值将与 [Attribute.configValue] 相同。
         */
        fun addByConfig(attribute: Attribute): Builder {
            return addByProvider(attribute, attribute.configValue())
        }

        /**
         * 添加一个属性，其 [AttributeInstance.getBaseValue] 的值由一个 [Double] 类型常量提供。
         */
        fun addByStatic(attribute: Attribute, value: Double): Builder {
            return addByProvider(attribute, provider(value))
        }

        /**
         * 添加一个属性，其 [AttributeInstance.getBaseValue] 的值由一个 [Double] 类型的 [Provider] 提供。
         */
        fun addByProvider(attribute: Attribute, value: Provider<Double>): Builder {
            val attributeInstance = create(attribute)
            attributeInstance.baseValue = value
            return this
        }

        fun build(): AttributeSupplier {
            return AttributeSupplier(builder)
        }

        private fun Attribute.configValue(): Provider<Double> {
            return if (this is ElementAttribute) {
                ATTRIBUTE_CONFIG.optionalEntry<Double>("default_attributes", entityType.name, element.uniqueId, descriptionId).orElse(defaultValueProvider)
            } else {
                ATTRIBUTE_CONFIG.optionalEntry<Double>("default_attributes", entityType.name, descriptionId).orElse(defaultValueProvider)
            }
        }
    }
}