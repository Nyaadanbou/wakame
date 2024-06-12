package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import team.unnamed.mocha.MochaEngine

/**
 * 基于 MoLang 表达式的技能条件
 */
interface MoLangCondition : NoCostSkillCondition {
    val eval: Evaluable<*>
    val failureMessage: Component

    companion object Factory : SkillConditionFactory<NoCostSkillCondition> {
        override fun provide(config: ConfigProvider): NoCostSkillCondition {
            val id = config.entry<String>("id")
            val eval = config.entry<Evaluable<*>>("eval")
            val priority =
                config.optionalEntry<SkillCondition.Priority>("priority").orElse(SkillCondition.Priority.NORMAL)
            val failureMessage = config.optionalEntry<Component>("failure_message").orElse(Component.empty())

            return Default(id, eval, priority, failureMessage)
        }
    }

    private class Default(
        id: Provider<String>,
        eval: Provider<Evaluable<*>>,
        priority: Provider<SkillCondition.Priority>,
        failureMessage: Provider<Component>
    ) : MoLangCondition, KoinComponent {
        private val engine: MochaEngine<*> by inject()

        override val id: String by id
        override val priority: SkillCondition.Priority by priority
        override val eval: Evaluable<*> by eval
        override val failureMessage: Component by failureMessage
        override val tagResolver: TagResolver = Placeholder.component(
            this.id,
            Component.text(this.eval.evaluate(engine))
        ) // TODO: Support MoLang tag resolver

        override fun test(context: SkillCastContext): Boolean {
            val result = eval.evaluate(engine)
            return result != 0.0
        }

        override fun notifyFailure(context: SkillCastContext) {
            val caster = context.optional(SkillCastContextKeys.CASTER_PLAYER) ?: return
            caster.bukkitPlayer.sendMessage(failureMessage)
        }
    }
}