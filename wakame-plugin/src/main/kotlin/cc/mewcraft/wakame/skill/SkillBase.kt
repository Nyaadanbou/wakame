package cc.mewcraft.wakame.skill

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import net.kyori.adventure.key.Key

/**
 * 包含 [Skill] 共同的实现.
 */
abstract class SkillBase(
    override val key: Key,
    private val config: ConfigProvider,
) : Skill {
    override val displays: SkillDisplay by config.optionalEntry<SkillDisplay>("displays").orElse(SkillDisplay.empty())
    override val conditions: SkillConditionGroup by config.optionalEntry<SkillConditionGroup>("conditions").orElse(SkillConditionGroup.empty())

    protected inner class TriggerConditionGetter {
        val forbiddenTriggers: Provider<TriggerConditions> = config.optionalEntry<TriggerConditions>("forbidden_triggers").orElse(TriggerConditions.empty())
        val interruptTriggers: Provider<TriggerConditions> = config.optionalEntry<TriggerConditions>("interrupt_triggers").orElse(TriggerConditions.empty())
    }

    protected object CasterUtil {
        fun <T : Caster.Single> getCaster(clazz: Class<T>, context: SkillContext): T? {
            val caster = context[SkillContextKey.CASTER]
            return caster?.value(clazz)
                ?: caster?.root(clazz)
        }

        inline fun <reified T : Caster.Single> getCaster(context: SkillContext): T? {
            return getCaster(T::class.java, context)
        }
    }

    protected object TargetUtil {
        fun getLocation(context: SkillContext): Target.Location? {
            return when (val target = context[SkillContextKey.TARGET]) {
                is Target.Location -> target
                is Target.LivingEntity -> TargetAdapter.adapt(target.bukkitEntity.location)
                else -> null
            }
        }
    }
}