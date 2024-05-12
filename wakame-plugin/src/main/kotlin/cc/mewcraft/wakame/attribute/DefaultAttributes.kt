package cc.mewcraft.wakame.attribute

import org.bukkit.entity.EntityType

/**
 * Provides default [AttributeInstance]s for various entity types.
 */
object DefaultAttributes {
    /**
     * This map stores Attribute Suppliers for each supported [EntityType].
     */
    private val ATTRIBUTE_SUPPLIERS: Map<EntityType, AttributeSupplier> = createSuppliers()

    private fun createSuppliers(): Map<EntityType, AttributeSupplier> {
        return emptyMap() // TODO 每个生物的属性的默认值现在由配置文件提供，这里应该是读取配置文件的逻辑
    }

    /**
     * Gets the [AttributeSupplier] for the [type] entity.
     *
     * **Currently, only limited entity types are supported.** Getting an
     * unsupported entity type will throw an [IllegalArgumentException].
     *
     * @param type the entity type to get AttributeSupplier
     * @return the attribute supplier
     * @throws IllegalArgumentException
     */
    fun getSupplier(type: EntityType): AttributeSupplier {
        return requireNotNull(ATTRIBUTE_SUPPLIERS[type]) { "Can't find attribute supplier for type $type" }
    }

    /**
     * Checks if the [type] entity has a default [AttributeSupplier].
     *
     * @param type the entity type to check
     * @return true if the entity type has a default supplier, false otherwise
     */
    fun hasSupplier(type: EntityType): Boolean {
        return ATTRIBUTE_SUPPLIERS.containsKey(type)
    }
}

class AttributeSupplierCreator {

}

/*
    //
    // Vanilla-backed Attributes
    //
    // Mechanics of these attributes are backed by vanilla game.

    addByConfig(Attributes.MAX_HEALTH)
    addByConfig(Attributes.MAX_ABSORPTION)
    addByConfig(Attributes.MOVEMENT_SPEED)
    addByConfig(Attributes.BLOCK_INTERACTION_RANGE)
    addByConfig(Attributes.ENTITY_INTERACTION_RANGE)

    //
    // Independent Attributes
    //
    // Mechanics of these attributes are implemented by ourselves.

    addByConfig(Attributes.ATTACK_EFFECT_CHANCE)
    addByConfig(Attributes.ATTACK_SPEED_LEVEL)
    addByConfig(Attributes.CRITICAL_STRIKE_CHANCE)
    addByConfig(Attributes.CRITICAL_STRIKE_POWER)
    addByConfig(Attributes.DAMAGE_REDUCTION_RATE)
    addByConfig(Attributes.LIFESTEAL)
    addByConfig(Attributes.LIFESTEAL_RATE)
    addByConfig(Attributes.MANASTEAL)
    addByConfig(Attributes.MANASTEAL_RATE)
    addByConfig(Attributes.MANA_REGENERATION)
    addByConfig(Attributes.HEALTH_REGENERATION)
    addByConfig(Attributes.MANA_CONSUMPTION_RATE)
    addByConfig(Attributes.MAX_MANA)

    //
    // Elemental Attributes
    //
    // Each of these attributes is associated with a certain element.
    // Mechanics of these attributes are implementation-defined.

    ElementRegistry.INSTANCES.objects.forEach {
        addByConfig(Attributes.byElement(it).DEFENSE)
        addByConfig(Attributes.byElement(it).DEFENSE_PENETRATION)
        addByConfig(Attributes.byElement(it).DEFENSE_PENETRATION_RATE)
        addByConfig(Attributes.byElement(it).MIN_ATTACK_DAMAGE)
        addByConfig(Attributes.byElement(it).MAX_ATTACK_DAMAGE)
    }
 */
