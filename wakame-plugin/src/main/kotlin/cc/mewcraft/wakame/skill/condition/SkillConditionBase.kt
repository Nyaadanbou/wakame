package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.*
import cc.mewcraft.wakame.skill.SkillSupport
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey

/**
 * 包含了 [SkillCondition] 实现类的共同逻辑.
 */
abstract class SkillConditionBase(
    conditionConfig: ConfigProvider
) : SkillCondition {
    /* 每个条件都有的数据配置 */
    final override val type: String by conditionConfig.entry<String>("type")
    private val config = PatchedConfigProvider(conditionConfig, SkillSupport.GLOBAL_SKILL_CONDITIONS.derive(type))

    /* 下面通过 PatchedConfig 来获取可覆盖的配置文件中的数据 */

    final override val priority: Int by config.optionalEntry<Int>("priority").orElse(0)

    /* 下面是一些用于实现 Session 的“组件” (参考: 组合设计模式) */

    /**
     * 该实现可以向施法者(Caster)发送预设的消息提示.
     */
    protected inner class Notification {
        private val successMessage: ConditionMessageGroup by config.entry<ConditionMessageGroup>("success_message")
        private val failureMessage: ConditionMessageGroup by config.entry<ConditionMessageGroup>("failure_message")

        /**
         * 发送条件满足时的消息提示.
         */
        fun notifySuccess(context: SkillContext) {
            val caster = context.optional(SkillContextKey.CASTER_AUDIENCE)
            if (caster != null) {
                successMessage.send(caster)
            }
        }

        /**
         * 发送条件不满足时的消息提示.
         */
        fun notifyFailure(context: SkillContext) {
            val caster = context.optional(SkillContextKey.CASTER_AUDIENCE)
            if (caster != null) {
                failureMessage.send(caster)
            }
        }
    }
}