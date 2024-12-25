package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.AbilityRegistry
import net.kyori.adventure.key.Key

interface AbilityProvider {
    fun get(): Ability
}

fun AbilityProvider(key: Key): AbilityProvider {
    return KeyedAbilityProvider(key)
}

private data class KeyedAbilityProvider(
    override val key: Key,
) : AbilityProvider, Keyed {
    override fun get(): Ability {
        return AbilityRegistry.INSTANCES[key]
    }
}
