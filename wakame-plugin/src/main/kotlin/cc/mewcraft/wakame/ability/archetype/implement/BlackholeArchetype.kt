package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.component.Blackhole
import cc.mewcraft.wakame.molang.Expression
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 选定一个位置为中心, 将该位置周围的怪物都吸引到该位置, 并造成伤害.
 */
object BlackholeArchetype : AbilityArchetype {
    override val key: Key = Identifiers.of("black_hole")
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val radius = config.node("radius").require<Expression>()
        val duration = config.node("duration").require<Expression>()
        val damage = config.node("damage").require<Expression>()
        return BlackholeInstance(key, config, radius, duration, damage)
    }
}

private class BlackholeInstance(
    key: Key,
    config: ConfigurationNode,
    val radius: Expression,
    val duration: Expression,
    val damage: Expression,
) : Ability(key, AbilityArchetypes.BLACKHOLE, config) {
    override fun configuration(): EntityCreateContext.(Entity) -> Unit = {
        it += Blackhole(radius, duration, damage)
    }
}
