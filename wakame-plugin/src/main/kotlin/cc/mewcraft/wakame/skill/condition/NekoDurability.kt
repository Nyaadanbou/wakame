package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode

/**
 * 检查 wakame 物品的耐久度.
 */
internal interface NekoDurability : SkillCondition {

    /**
     * 剩余耐久度大于该值, 则条件满足.
     */
    val durability: Evaluable<*>

    companion object Factory : SkillConditionFactory<NekoDurability> {
        override fun create(config: ConfigurationNode): NekoDurability {
            return Impl(config)
        }
    }

    private class Impl(
        config: ConfigurationNode,
    ) : SkillConditionBase(config), NekoDurability {

        override val durability: Evaluable<*> = config.node("durability").krequire<Evaluable<*>>()
        override val resolver: TagResolver = Placeholder.component(this.type, Component.text(this.durability.evaluate()))

        override fun newSession(context: SkillContext): SkillConditionSession {
            val nekoStack = context[SkillContextKey.NEKO_STACK] ?: return SkillConditionSession.alwaysFailure()
            val damageable = nekoStack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return SkillConditionSession.alwaysFailure()
            val engine = context.getOrThrow(SkillContextKey.MOCHA_ENGINE)
            val evaluatedDurability = this.durability.evaluate(engine).toStableInt()
            val isSuccess = (damageable.maxDamage - damageable.damage) <= evaluatedDurability
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillContext) {
                val nekoStack = context[SkillContextKey.ITEM_STACK]?.tryNekoStack ?: return
                val damageable = nekoStack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return
                val engine = context.getOrThrow(SkillContextKey.MOCHA_ENGINE)
                val evaluatedDurability = durability.evaluate(engine).toStableInt()
                val newDamage = damageable.copy(damage = damageable.damage + evaluatedDurability)
                nekoStack.components.set(ItemComponentTypes.DAMAGEABLE, newDamage)
                notification.notifySuccess(context)
            }

            override fun onFailure(context: SkillContext) {
                notification.notifyFailure(context)
            }
        }
    }
}