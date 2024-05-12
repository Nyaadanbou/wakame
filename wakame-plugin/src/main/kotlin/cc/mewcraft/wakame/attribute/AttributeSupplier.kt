package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.registry.ENTITY_CONFIG_FILE
import org.bukkit.attribute.Attributable
import org.bukkit.entity.EntityType
import java.util.UUID

private val ENTITY_ATTRIBUTE_CONFIG by lazy { Configs.YAML[ENTITY_CONFIG_FILE].node("entity_attributes") }

fun AttributeSupplier(entityType: EntityType, builder: AttributeSupplierBuilder.() -> Unit): AttributeSupplier {
    return AttributeSupplierBuilder(entityType).apply(builder).build()
}

/**
 * Responsible to provide default attribute values.
 *
 * @property prototypes The available attribute instances in this supplier.
 * @property attributeTypes The available attribute types in this supplier.
 */
class AttributeSupplier internal constructor(
    private val prototypes: Map<Attribute, AttributeInstance>,
) {
    val attributeTypes: Collection<Attribute> = prototypes.keys

    fun createAttributeInstance(attribute: Attribute, attributable: Attributable): AttributeInstance? {
        val prototype = prototypes[attribute] ?: return null
        val product = AttributeInstanceFactory.createInstance(attribute, attributable)
        product.replace(prototype)
        return product
    }

    fun getValue(attribute: Attribute): Double {
        return getPrototypeOrThrow(attribute).getValue()
    }

    fun getBaseValue(attribute: Attribute): Double {
        return getPrototypeOrThrow(attribute).getBaseValue()
    }

    fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        val modifier = requireNotNull(getPrototypeOrThrow(attribute).getModifier(uuid)) {
            "Can't find attribute modifier '$uuid' on attribute '${attribute.descriptionId}'"
        }
        return modifier.amount
    }

    fun hasAttribute(type: Attribute): Boolean {
        return prototypes.containsKey(type)
    }

    fun hasModifier(type: Attribute, uuid: UUID): Boolean {
        return prototypes[type]?.getModifier(uuid) != null
    }

    private fun getPrototypeOrThrow(attribute: Attribute): AttributeInstance {
        return requireNotNull(prototypes[attribute]) {
            "Can't find attribute instance for attribute '${attribute.descriptionId}'"
        }
    }
}

class AttributeSupplierBuilder(
    private val entityType: EntityType,
) {
    private val prototypes: MutableMap<Attribute, AttributeInstance> = HashMap()

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于实体的配置文件。
     */
    fun addByConfig(attribute: Attribute): AttributeSupplierBuilder {
        // TODO add support for MythicMobs entities
        val provider = if (attribute is ElementAttribute) {
            ENTITY_ATTRIBUTE_CONFIG.optionalEntry<Double>(entityType.key().value(), attribute.facadeId, attribute.element.uniqueId).orElse(attribute.defaultValue)
        } else {
            ENTITY_ATTRIBUTE_CONFIG.optionalEntry<Double>(entityType.key().value(), attribute.facadeId).orElse(attribute.defaultValue)
        }
        return addByProvider(attribute, provider)
    }

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于给定的常量。
     *
     * 一般情况下，你不会直接用到这个。
     */
    fun addByStatic(attribute: Attribute, value: Double): AttributeSupplierBuilder {
        return addByProvider(attribute, provider(value))
    }

    /**
     * 添加一个供应者，其 [AttributeInstance.getBaseValue] 的值将基于给定的 provider。
     *
     * 一般情况下，你不会直接用到这个。
     */
    fun addByProvider(attribute: Attribute, value: Provider<Double>): AttributeSupplierBuilder {
        val prototype = AttributeInstanceFactory.createPrototype(attribute)
        prototype.setBaseValue(value.get())
        prototypes[attribute] = prototype
        return this
    }

    fun build(): AttributeSupplier {
        return AttributeSupplier(prototypes)
    }
}

class AttributeSupplierDSL {

}