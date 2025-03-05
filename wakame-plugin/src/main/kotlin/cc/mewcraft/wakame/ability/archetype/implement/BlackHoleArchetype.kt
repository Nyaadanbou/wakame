package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.component.BlackHole
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 选定一个位置为中心, 将该位置周围的怪物都吸引到该位置, 并造成伤害.
 */
object BlackHoleArchetype : AbilityArchetype {
    override val key: Key = Identifiers.of("black_hole")
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val radius = config.node("radius").require<Evaluable<*>>()
        val duration = config.node("duration").require<Evaluable<*>>()
        val damage = config.node("damage").require<Evaluable<*>>()
        return BlackHoleInstance(key, config, radius, duration, damage)
    }
}

private class BlackHoleInstance(
    key: Key,
    config: ConfigurationNode,
    val radius: Evaluable<*>,
    val duration: Evaluable<*>,
    val damage: Evaluable<*>,
) : Ability(key, AbilityArchetypes.BLACK_HOLE, config) {
    override fun configuration(): EntityCreateContext.(Entity) -> Unit = {
        it += BlackHole(radius, duration, damage)
    }
}