package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.FixedSkillCastResult
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.SkillCastResult
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.key.Key

interface Dash : Skill {

    /**
     * 冲刺的距离.
     */
    val distance: Double

    companion object Factory : SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigProvider): Dash {
            val distance = config.entry<Double>("distance")
            return DefaultImpl(key, config, distance)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        distance: Provider<Double>,
    ) : Dash, SkillBase(key, config) {

        override val distance: Double by distance

        override fun cast(context: SkillCastContext): SkillCastResult {
            val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return FixedSkillCastResult.NONE_CASTER
            val direction = player.location.direction.normalize()
            val velocity = direction.multiply(distance)
            player.velocity = velocity
            return FixedSkillCastResult.SUCCESS
        }
    }
}