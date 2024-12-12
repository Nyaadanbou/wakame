package cc.mewcraft.wakame.skill2.condition

import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill2.context.skillContext
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode

/**
 * 检查 MoLang 表达式.
 */
interface MoLangExpression : SkillCondition {

    /**
     * 表达式返回 `不为零`, 则条件满足.
     */
    val evaluable: Evaluable<*>

    companion object Factory : SkillConditionFactory<MoLangExpression> {
        override fun create(config: ConfigurationNode): MoLangExpression {
            return Impl(config)
        }
    }

    private class Impl(
        config: ConfigurationNode,
    ) : MoLangExpression, SkillConditionBase(config) {

        override val evaluable: Evaluable<*> = config.node("eval").krequire<Evaluable<*>>()
        override val resolver: TagResolver = Placeholder.component(this.type, Component.text(this.evaluable.evaluate(MoLangSupport.createEngine()))) // TODO: Support MoLang tag resolver

        override fun newSession(componentMap: ComponentMap): SkillConditionSession {
            val context = skillContext(componentMap)
            val engine = context.mochaEngine
            val isSuccess = evaluable.evaluate(engine) != .0
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(componentMap: ComponentMap) {
                val context = skillContext(componentMap)
                // 用预设的实现发消息
                notification.notifySuccess(context)

                // 自定义的逻辑
                context.caster.player?.heal(2.0)
            }

            override fun onFailure(componentMap: ComponentMap) {
                val context = skillContext(componentMap)
                // 用预设的实现发消息
                notification.notifyFailure(context)

                // 自定义的逻辑
                context.caster.player?.damage(2.0)
            }
        }
    }
}