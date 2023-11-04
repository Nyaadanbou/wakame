package cc.mewcraft.wakame.attribute.handler

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A map tied to a player, containing current attributes of the player. Not thread-safe.
 *
 * States of this object should be periodically updated by [PlayerAttributeAccessor].
 */
class PlayerAttributeMap : KoinComponent { // TODO optimize its performance
    private val attributeRegistry: AttributeRegistry by inject()

    // Abilities
    private val abilities: MutableMap<String, Ability> = HashMap()

    // Attribute bases
    private val attributeBases: MutableMap<String, Attribute> = HashMap()

    // Attribute modifiers
    private val attributeModifiers: MutableMap<String, List<AttributeModifier>> = HashMap()

    // Current resources of this player
    val resourceContainer: PlayerResourceContainer = PlayerResourceContainer()

    fun editAttributeModifier(key: Key) {

    }

    fun <T : Attribute> getAttributeValue(key: String): Int {
        val attribute = attributeBases.computeIfAbsent(key) { attributeRegistry.getDefault<T>(key) }
        val modifiers = attributeModifiers.computeIfAbsent(key) { ArrayList() }
        return attribute.compute(modifiers)
    }
}
