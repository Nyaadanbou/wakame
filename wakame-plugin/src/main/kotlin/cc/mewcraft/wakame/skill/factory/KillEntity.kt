package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.FixedSkillCastResult
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.SkillCastResult
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.key.Key

interface KillEntity : Skill {
    companion object Factory : SkillFactory<KillEntity> {
        override fun create(key: Key, config: ConfigProvider): KillEntity {
            return DefaultImpl(key, config)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
    ) : KillEntity, SkillBase(key, config) {

        override fun cast(context: SkillCastContext): SkillCastResult {
            val entity = context.optional(SkillCastContextKey.TARGET_LIVING_ENTITY)?.bukkitEntity ?: return FixedSkillCastResult.NONE_TARGET
            entity.health = 0.0
            return FixedSkillCastResult.SUCCESS
        }
    }
}