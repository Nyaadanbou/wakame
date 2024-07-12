package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.TriggerConditions
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import net.kyori.adventure.key.Key

interface Lightning : Skill {

    companion object Factory : SkillFactory<Lightning> {
        override fun create(key: Key, config: ConfigProvider): Lightning {
            return DefaultImpl(key, config)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
    ) : Lightning, SkillBase(key, config) {
        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick {
            return LightningTick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class LightningTick(
            context: SkillContext,
            override val interruptTriggers: TriggerConditions,
            override val forbiddenTriggers: TriggerConditions
        ): AbstractPlayerSkillTick(this@DefaultImpl, context) {
            override fun tickCast(tickCount: Long): TickResult {
                val location = TargetUtil.getLocation(context)?.bukkitLocation ?: return TickResult.INTERRUPT
                val world = location.world
                val lightning = world.strikeLightning(location)
                return TickResult.ALL_DONE
            }
        }
    }
}