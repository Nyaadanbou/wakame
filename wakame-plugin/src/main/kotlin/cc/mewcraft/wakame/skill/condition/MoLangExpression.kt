package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent

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
    ) : KoinComponent, MoLangExpression, SkillConditionBase(config) {

        override val evaluable: Evaluable<*> by config.entry<Evaluable<*>>("eval")
        override val resolver: TagResolver = Placeholder.component(this.id, Component.text(this.evaluable.evaluate(getKoin().get()))) // TODO: Support MoLang tag resolver

        override fun newSession(context: SkillCastContext): SkillConditionSession {
            val engine = MoLangSupport.createEngine()
            // TODO: engine.bindInstance(...)
            val isSuccess = evaluable.evaluate(engine) != .0
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillCastContext) {
                // 用预设的实现发消息
                notification.notifySuccess(context)

                // 自定义的逻辑
                context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer?.heal(2.0)
            }

            override fun onFailure(context: SkillCastContext) {
                // 用预设的实现发消息
                notification.notifyFailure(context)

                // 自定义的逻辑
                context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer?.damage(2.0)
            }
        }
    }
}