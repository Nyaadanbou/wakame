package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 检查 wakame 物品的耐久度.
 */
internal interface NekoDurability : SkillCondition {

    /**
     * 剩余耐久度大于该值, 则条件满足.
     */
    val durability: Evaluable<*>

    companion object Factory : SkillConditionFactory<NekoDurability> {
        override fun create(config: ConfigProvider): NekoDurability {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigProvider,
    ) : SkillConditionBase(config), NekoDurability {

        override val durability: Evaluable<*> by config.entry<Evaluable<*>>("durability")
        override val resolver: TagResolver = Placeholder.component(this.type, Component.text(this.durability.evaluate()))

        override fun newSession(context: SkillCastContext): SkillConditionSession {
            val nekoStack = context.optional(SkillCastContextKey.NEKO_STACK) ?: return SkillConditionSession.alwaysFailure()
            val damageable = nekoStack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return SkillConditionSession.alwaysFailure()
            val engine = context.get(SkillCastContextKey.MOCHA_ENGINE)
            val evaluatedDurability = this.durability.evaluate(engine).toStableInt()
            val isSuccess = (damageable.maxDamage - damageable.damage) <= evaluatedDurability
            return SessionImpl(isSuccess)
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillCastContext) {
                val nekoStack = context.optional(SkillCastContextKey.ITEM_STACK)?.tryNekoStack ?: return
                val damageable = nekoStack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return
                val engine = context.get(SkillCastContextKey.MOCHA_ENGINE)
                val evaluatedDurability = durability.evaluate(engine).toStableInt()
                val newDamage = damageable.copy(damage = damageable.damage + evaluatedDurability)
                nekoStack.components.set(ItemComponentTypes.DAMAGEABLE, newDamage)
                notification.notifySuccess(context)
            }

            override fun onFailure(context: SkillCastContext) {
                notification.notifyFailure(context)
            }
        }
    }
}