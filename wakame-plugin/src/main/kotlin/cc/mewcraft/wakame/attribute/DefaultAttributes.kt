package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.registry.ATTRIBUTE_CONFIG_FILE
import cc.mewcraft.wakame.registry.ElementRegistry
import org.bukkit.entity.EntityType

private val ATTRIBUTE_CONFIG by lazy { Configs.YAML[ATTRIBUTE_CONFIG_FILE] }

/**
 * Provides default [AttributeInstance]s for various entity types.
 */
object DefaultAttributes {
    private val DEFAULT_SUPPLIERS: Map<EntityType, AttributeSupplier> = buildMap {
        put(EntityType.PLAYER, AttributeSupplier {
            //
            // Vanilla-backed Attributes
            //
            // Mechanics of these attributes are backed by vanilla game.

            add(Attributes.MAX_HEALTH) { configValue(EntityType.PLAYER) }
            add(Attributes.MAX_ABSORPTION) { configValue(EntityType.PLAYER) }
            add(Attributes.MOVEMENT_SPEED) { configValue(EntityType.PLAYER) }
            add(Attributes.BLOCK_INTERACTION_RANGE) { configValue(EntityType.PLAYER) }
            add(Attributes.ENTITY_INTERACTION_RANGE) { configValue(EntityType.PLAYER) }

            //
            // Independent Attributes
            //
            // Mechanics of these attributes are implemented by ourselves.

            add(Attributes.ATTACK_EFFECT_CHANCE) { configValue(EntityType.PLAYER) }
            add(Attributes.ATTACK_SPEED_LEVEL) { configValue(EntityType.PLAYER) }
            add(Attributes.CRITICAL_STRIKE_CHANCE) { configValue(EntityType.PLAYER) }
            add(Attributes.CRITICAL_STRIKE_POWER) { configValue(EntityType.PLAYER) }
            add(Attributes.DAMAGE_REDUCTION_RATE) { configValue(EntityType.PLAYER) }
            add(Attributes.LIFESTEAL) { configValue(EntityType.PLAYER) }
            add(Attributes.LIFESTEAL_RATE) { configValue(EntityType.PLAYER) }
            add(Attributes.MANASTEAL) { configValue(EntityType.PLAYER) }
            add(Attributes.MANASTEAL_RATE) { configValue(EntityType.PLAYER) }
            add(Attributes.MANA_REGENERATION) { configValue(EntityType.PLAYER) }
            add(Attributes.HEALTH_REGENERATION) { configValue(EntityType.PLAYER) }
            add(Attributes.MANA_CONSUMPTION_RATE) { configValue(EntityType.PLAYER) }
            add(Attributes.MAX_MANA) { configValue(EntityType.PLAYER) }

            //
            // Elemental Attributes
            //
            // Each of these attributes is associated with a certain element.
            // Mechanics of these attributes are implementation-defined.

            ElementRegistry.INSTANCES.objects.forEach {
                add(Attributes.byElement(it).DEFENSE) { configValue(EntityType.PLAYER) }
                add(Attributes.byElement(it).DEFENSE_PENETRATION) { configValue(EntityType.PLAYER) }
                add(Attributes.byElement(it).DEFENSE_PENETRATION_RATE) { configValue(EntityType.PLAYER) }
                add(Attributes.byElement(it).MIN_ATTACK_DAMAGE) { configValue(EntityType.PLAYER) }
                add(Attributes.byElement(it).MAX_ATTACK_DAMAGE) { configValue(EntityType.PLAYER) }
            }
        })
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

    private fun Attribute.configValue(type: EntityType): Provider<Double> {
        return if (this is ElementAttribute) {
            ATTRIBUTE_CONFIG.optionalEntry<Double>("default_attributes", type.name, element.uniqueId, descriptionId).orElse(defaultValue)
        } else {
            ATTRIBUTE_CONFIG.optionalEntry<Double>("default_attributes", type.name, descriptionId).orElse(defaultValue)
        }
    }
}