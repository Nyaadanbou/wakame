package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.value
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 检查 MoLang 表达式.
 */
interface MoLangExpression : SkillCondition {

    /**
     * 表达式返回 `不为零`, 则条件满足.
     */
    val evaluable: Evaluable<*>

    companion object Factory : SkillConditionFactory<MoLangExpression> {
        override fun create(config: ConfigProvider): MoLangExpression {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigProvider,
    ) : MoLangExpression, SkillConditionBase(config) {

        override val evaluable: Evaluable<*> by config.entry<Evaluable<*>>("eval")
        override val resolver: TagResolver = Placeholder.component(this.type, Component.text(this.evaluable.evaluate(MoLangSupport.createEngine()))) // TODO: Support MoLang tag resolver

        override fun newSession(context: SkillContext): SkillConditionSession {
            val engine = MoLangSupport.createEngine()
            // TODO: engine.bindInstance(...)
            val isSuccess = evaluable.evaluate(engine) != .0
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillContext) {
                // 用预设的实现发消息
                notification.notifySuccess(context)

                // 自定义的逻辑
                context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer?.heal(2.0)
            }

            override fun onFailure(context: SkillContext) {
                // 用预设的实现发消息
                notification.notifyFailure(context)

                // 自定义的逻辑
                context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer?.damage(2.0)
            }
        }
    }
}