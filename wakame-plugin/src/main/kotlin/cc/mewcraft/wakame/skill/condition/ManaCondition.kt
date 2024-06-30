package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
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

        override fun newSession(context: SkillCastContext): SkillConditionSession {
            val user = context.optional(SkillCastContextKey.USER) ?: return SkillConditionSession.alwaysFailure()
            val engine = context.get(SkillCastContextKey.MOCHA_ENGINE)
            val isSuccess = user.resourceMap.current(ResourceTypeRegistry.MANA) >= mana.evaluate(engine)
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillCastContext) {
                val user = context.get(SkillCastContextKey.USER)
                val engine = context.get(SkillCastContextKey.MOCHA_ENGINE)
                user.resourceMap.take(ResourceTypeRegistry.MANA, mana.evaluate(engine).toStableInt())
                notification.notifySuccess(context)
            }

            override fun onFailure(context: SkillCastContext) {
                notification.notifyFailure(context)
            }
        }

    }
}