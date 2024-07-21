package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode

/**
 * 检查玩家的魔法值.
 */
interface ManaCondition : SkillCondition {

    /**
     * 魔法值大于该值, 则条件满足.
     */
    val mana: Evaluable<*>

    companion object : SkillConditionFactory<ManaCondition> {
        override fun create(config: ConfigurationNode): ManaCondition {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigurationNode,
    ) : ManaCondition, SkillConditionBase(config) {

        override val mana: Evaluable<*> = config.node("mana").krequire()
        override val resolver: TagResolver = TagResolver.empty()

        override fun newSession(context: SkillContext): SkillConditionSession {
            val user = context[SkillContextKey.USER] ?: return SkillConditionSession.alwaysFailure()
            val engine = context.getOrThrow(SkillContextKey.MOCHA_ENGINE)
            val isSuccess = user.resourceMap.current(ResourceTypeRegistry.MANA) >= mana.evaluate(engine)
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillContext) {
                val user = context.getOrThrow(SkillContextKey.USER)
                val engine = context.getOrThrow(SkillContextKey.MOCHA_ENGINE)
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