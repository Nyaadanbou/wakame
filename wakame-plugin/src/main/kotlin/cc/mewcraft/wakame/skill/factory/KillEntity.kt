package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

interface KillEntity : Skill {
    companion object Factory : SkillFactory<KillEntity> {
        override fun create(key: Key, config: ConfigurationNode): KillEntity {
            return Impl(key, config)
        }
    }

    private class Impl(
        override val key: Key,
        config: ConfigurationNode,
    ) : KillEntity, SkillBase(key, config) {
        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<KillEntity> {
            return Tick(context, this, triggerConditionGetter.interrupt, triggerConditionGetter.forbidden)
        }
    }
}

private class Tick(
    context: SkillContext,
    skill: KillEntity,
    override val interruptTriggers: TriggerConditions,
    override val forbiddenTriggers: TriggerConditions
) : AbstractPlayerSkillTick<KillEntity>(skill, context) {

    override fun tickCastPoint(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("杀死生物前摇awa")
        return TickResult.ALL_DONE
    }

    override fun tickBackswing(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("杀死生物后摇qwq")
        return TickResult.ALL_DONE
    }

    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val entity = context[SkillContextKey.TARGET]?.value<Target.LivingEntity>()?.bukkitEntity ?: return TickResult.INTERRUPT
        entity.health = 0.0
        return TickResult.ALL_DONE
    }
}