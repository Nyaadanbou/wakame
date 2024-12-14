package cc.mewcraft.wakame.skill2.condition

import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.skill2.context.skillInput
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
            return Impl(config)
        }
    }

    private class Impl(
        config: ConfigurationNode,
    ) : ManaCondition, SkillConditionBase(config) {

        override val mana: Evaluable<*> = config.node("mana").krequire()
        override val resolver: TagResolver = TagResolver.empty()

        override fun newSession(componentMap: ComponentMap): SkillConditionSession {
            val context = skillInput(componentMap)
            val user = context.user ?: return SkillConditionSession.alwaysFailure()
            val engine = context.mochaEngine
            val isSuccess = user.resourceMap.current(ResourceTypeRegistry.MANA) >= mana.evaluate(engine)
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(componentMap: ComponentMap) {
                val context = skillInput(componentMap)
                val user = context.user ?: error("User not found")
                val engine = context.mochaEngine
                val value = mana.evaluate(engine).toStableInt()
                user.resourceMap.take(ResourceTypeRegistry.MANA, value)
                notification.notifySuccess(context)
            }

            override fun onFailure(componentMap: ComponentMap) {
                val context = skillInput(componentMap)
                notification.notifyFailure(context)
            }
        }

    }
}