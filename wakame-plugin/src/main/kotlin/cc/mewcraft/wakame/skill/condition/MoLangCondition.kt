package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.molang.Evaluable
import net.kyori.adventure.text.Component
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
            val priority = config.optionalEntry<SkillCondition.Priority>("priority").orElse(SkillCondition.Priority.NORMAL)
            val eval = config.entry<Evaluable<*>>("eval")
            val failureMessage = config.optionalEntry<Component>("failure_message").orElse(Component.empty())

            return Default(priority, eval, failureMessage)
        }
    }

    private class Default(
        priority: Provider<SkillCondition.Priority>,
        eval: Provider<Evaluable<*>>,
        failureMessage: Provider<Component>
    ) : MoLangCondition, KoinComponent {
        private val engine: MochaEngine<*> by inject()

        override val priority: SkillCondition.Priority by priority
        override val eval: Evaluable<*> by eval
        override val failureMessage: Component by failureMessage

        override fun test(context: SkillCastContext): Boolean {
            val result = eval.evaluate(engine)
            return result != 0.0
        }

        override fun notifyFailure(context: SkillCastContext) {
            context.player.sendMessage(failureMessage)
        }
    }
}