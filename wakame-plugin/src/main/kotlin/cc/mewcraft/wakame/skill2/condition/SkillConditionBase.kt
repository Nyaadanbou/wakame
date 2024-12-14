package cc.mewcraft.wakame.skill2.condition

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.config.PatchedConfigurationNode
import cc.mewcraft.wakame.skill2.SkillSupport
import cc.mewcraft.wakame.skill2.context.SkillInput
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 包含了 [SkillCondition] 实现类的共同逻辑.
 */
abstract class SkillConditionBase(
    conditionConfig: ConfigurationNode,
) : SkillCondition {
    /* 每个条件都有的数据配置 */
    final override val type: String = conditionConfig.node("type").krequire<String>()
    private val config = PatchedConfigurationNode(conditionConfig, SkillSupport.GLOBAL_SKILL_CONDITIONS.get().node(type))

    /* 下面通过 PatchedConfig 来获取可覆盖的配置文件中的数据 */

    final override val priority: Int = config.node("priority").get<Int>() ?: 0

    /* 下面是一些用于实现 Session 的“组件” (参考: 组合设计模式) */

    /**
     * 该实现可以向施法者(Caster)发送预设的消息提示.
     */
    protected inner class Notification {
        private val successMessage: AudienceMessageGroup = config.node("success_message").krequire()
        private val failureMessage: AudienceMessageGroup = config.node("failure_message").krequire()

        /**
         * 发送条件满足时的消息提示.
         */
        fun notifySuccess(context: SkillInput) {
            val caster = context.caster
            successMessage.send(caster.entity)
        }

        /**
         * 发送条件不满足时的消息提示.
         */
        fun notifyFailure(context: SkillInput) {
            val caster = context.caster
            failureMessage.send(caster.entity)
        }
    }
}