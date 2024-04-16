package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry.ElementRegistry
import org.bukkit.entity.EntityType

/**
 * Provides default [AttributeInstance]s for various entity types.
 */
object DefaultAttributes {
    private val DEFAULT_SUPPLIERS: Map<EntityType, AttributeSupplier> = buildMap {
        putSupplier(EntityType.PLAYER) {
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
        }
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
        return requireNotNull(DEFAULT_SUPPLIERS[type]) { "Can't find attribute supplier for type $type" }
    }

    /**
     * Checks if the [type] entity has a default [AttributeSupplier].
     *
     * @param type the entity type to check
     * @return true if the entity type has a default supplier, false otherwise
     */
    fun hasSupplier(type: EntityType): Boolean {
        return DEFAULT_SUPPLIERS.containsKey(type)
    }

    /* Private Functions */

    private fun MutableMap<EntityType, AttributeSupplier>.putSupplier(entityType: EntityType, builder: AttributeSupplier.Builder.() -> Unit) {
        put(entityType, AttributeSupplier(entityType, builder))
    }
}