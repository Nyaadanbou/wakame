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
 * It may **not** provide default values for every attribute type!
 *
 * @property prototypes The available [AttributeInstance]s in this supplier.
 * @property attributes The available [Attribute]s in this supplier.
 */
class AttributeSupplier internal constructor(
    private val prototypes: Map<Attribute, AttributeInstance>,
) {
    val attributes: Collection<Attribute> = prototypes.keys

    /**
     * Creates a new [AttributeInstance] from this supplier.
     *
     * Returns `null` if the [attribute] is not present in this supplier.
     *
     * This function may have **side effect** to the [attributable].

     * @param attribute the attribute type
     * @param attributable the object which holds the vanilla attribute instances in the world state
     * @return a new instance of [AttributeInstance].
     */
    fun createAttributeInstance(attribute: Attribute, attributable: Attributable): AttributeInstance? {
        val prototype = prototypes[attribute] ?: return null
        val product = AttributeInstanceFactory.createInstance(attribute, attributable)
        product.replace(prototype)
        return product
    }

    /**
     * Gets the value for the [attribute] after all modifiers have been applied.
     *
     * @param attribute the attribute type
     * @return the value for the attribute
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getValue(attribute: Attribute): Double {
        return getPrototypeOrThrow(attribute).getValue()
    }

    /**
     * Gets the base value for the [attribute].
     *
     * @param attribute the attribute type
     * @return the base value for the attribute
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getBaseValue(attribute: Attribute): Double {
        return getPrototypeOrThrow(attribute).getBaseValue()
    }

    /**
     * Gets the modifier value specified by [attribute] and [uuid].
     *
     * @param attribute the attribute type
     * @param uuid the uuid
     * @return the modifier value
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        return requireNotNull(getPrototypeOrThrow(attribute).getModifier(uuid)) {
            "Can't find attribute modifier '$uuid' on attribute '${attribute.descriptionId}'"
        }.amount
    }

    /**
     * Checks whether this supplier has the [type].
     *
     * @param type the attribute type
     * @return `true` if this supplier has the [type]
     * @throws IllegalArgumentException if the [type] is not present in this supplier
     */
    fun hasAttribute(type: Attribute): Boolean {
        return prototypes.containsKey(type)
    }

    /**
     * Checks whether this supplier has the modifier specified by [type] and [uuid].
     *
     * @param type the attribute type
     * @param uuid the uuid
     * @return `true` if this supplier has the modifier
     */
    fun hasModifier(type: Attribute, uuid: UUID): Boolean {
        return prototypes[type]?.getModifier(uuid) != null
    }

    private fun getPrototypeOrThrow(attribute: Attribute): AttributeInstance {
        return requireNotNull(prototypes[attribute]) {
            "Can't find attribute instance for attribute '${attribute.descriptionId}'"
        }
    }
}

/**
 * The builder of [AttributeSupplier].
 *
 * @property entityType the living entity for the builder
 */
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
        prototype.setBaseValue(value.get()) // FIXME 这个实际上会导致数值无法重载
        prototypes[attribute] = prototype
        return this
    }

    fun build(): AttributeSupplier {
        return AttributeSupplier(prototypes)
    }
}

class AttributeSupplierDSL {

}