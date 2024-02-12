package cc.mewcraft.wakame.attribute.base

import cc.mewcraft.wakame.registry.ElementRegistry
import org.bukkit.entity.EntityType

/**
 * Provides default [AttributeInstance]s for various entity types.
 */
object DefaultAttributes {
    private val DEFAULT_SUPPLIERS: Map<EntityType, AttributeSupplier> = buildMap {
        ////// PLAYER //////
        put(
            EntityType.PLAYER,
            attributeSupplier {
                /*
                  ////// Vanilla-backed Attributes //////

                  Mechanics of these attributes are backed by vanilla game.
                */

                add(Attributes.MAX_HEALTH)
                add(Attributes.MAX_ABSORPTION)
                add(Attributes.MOVEMENT_SPEED_RATE)
                add(Attributes.BLOCK_INTERACTION_RANGE)
                add(Attributes.ENTITY_INTERACTION_RANGE)

                /*
                  ////// Independent Attributes //////

                  Mechanics of these attributes are implemented by ourselves.
                */

                add(Attributes.ATTACK_EFFECT_CHANCE)
                add(Attributes.ATTACK_SPEED_LEVEL)
                add(Attributes.CRITICAL_STRIKE_CHANCE)
                add(Attributes.CRITICAL_STRIKE_POWER)
                add(Attributes.DAMAGE_TAKEN_RATE)
                add(Attributes.LIFESTEAL)
                add(Attributes.LIFESTEAL_RATE)
                add(Attributes.MANASTEAL)
                add(Attributes.MANASTEAL_RATE)
                add(Attributes.MANA_REGENERATION)
                add(Attributes.HEALTH_REGENERATION)
                add(Attributes.MANA_CONSUMPTION_RATE)
                add(Attributes.MAX_MANA)

                /*
                  ////// Elemental Attributes //////

                  Each of these attributes is associated with a certain element.
                  Mechanics of these attributes are implementation-defined.
                */

                ElementRegistry.values.forEach {
                    add(Attributes.byElement(it).DEFENSE)
                    add(Attributes.byElement(it).DEFENSE_PENETRATION)
                    add(Attributes.byElement(it).DEFENSE_PENETRATION_RATE)
                    add(Attributes.byElement(it).MIN_ATTACK_DAMAGE)
                    add(Attributes.byElement(it).MAX_ATTACK_DAMAGE)
                    add(Attributes.byElement(it).ATTACK_DAMAGE_RATE)
                }
            }
        )

        // TODO MONSTER
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

    fun hasSupplier(type: EntityType): Boolean {
        return DEFAULT_SUPPLIERS.containsKey(type)
    }
}