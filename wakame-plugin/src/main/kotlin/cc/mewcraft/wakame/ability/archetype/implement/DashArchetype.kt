package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.component.Dash
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import com.github.quillraven.fleks.EntityCreateContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 冲刺技能.
 */
object DashArchetype : AbilityArchetype {
    override val key: Key = Identifiers.of("dash")
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val stepDistance = config.node("step_distance").require<Double>()
        val duration = config.node("duration").get<Long>() ?: 50
        val canContinueAfterHit = config.node("can_continue_after_hit").get<Boolean>() ?: true
        val hitEffect = config.node("hit_effects").get<List<Ability>>() ?: emptyList()
        return DashInstance(key, config, stepDistance, duration, canContinueAfterHit, hitEffect)
    }
}

private class DashInstance(
    key: Key,
    config: ConfigurationNode,
    val stepDistance: Double,
    val duration: Long,
    val canContinueAfterHit: Boolean,
    val hitEffects: List<Ability>,
) : Ability(key, AbilityArchetypes.DASH, config) {
    override fun configuration(): EntityCreateContext.(com.github.quillraven.fleks.Entity) -> Unit = {
        it += Dash(stepDistance, duration, canContinueAfterHit, hitEffects)
    }
}