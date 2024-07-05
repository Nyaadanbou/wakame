package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 检查玩家的魔法值.
 */
interface ManaCondition : SkillCondition {

    /**
     * 魔法值大于该值, 则条件满足.
     */
    val mana: Evaluable<*>

    companion object : SkillConditionFactory<ManaCondition> {
        override fun create(config: ConfigProvider): ManaCondition {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigProvider,
    ) : ManaCondition, SkillConditionBase(
        config
    ) {

        override val mana: Evaluable<*> by config.entry<Evaluable<*>>("mana")
        override val resolver: TagResolver = TagResolver.empty()

        override fun newSession(context: SkillContext): SkillConditionSession {
            val user = context.optional(SkillContextKey.USER) ?: return SkillConditionSession.alwaysFailure()
            val engine = context.get(SkillContextKey.MOCHA_ENGINE)
            val isSuccess = user.resourceMap.current(ResourceTypeRegistry.MANA) >= mana.evaluate(engine)
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillContext) {
                val user = context.get(SkillContextKey.USER)
                val engine = context.get(SkillContextKey.MOCHA_ENGINE)
                val value = mana.evaluate(engine).toStableInt()
                user.resourceMap.take(ResourceTypeRegistry.MANA, value)
                notification.notifySuccess(context)
            }

            override fun onFailure(context: SkillContext) {
                notification.notifyFailure(context)
            }
        }

    }
}