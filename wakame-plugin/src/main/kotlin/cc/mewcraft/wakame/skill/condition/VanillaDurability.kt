package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.inventory.meta.Damageable

/**
 * 检查原版物品的耐久度.
 */
interface VanillaDurability : SkillCondition {

    /**
     * 剩余耐久度大于该值, 则条件满足.
     */
    val durability: Evaluable<*>

    companion object : SkillConditionFactory<VanillaDurability> {
        override fun create(config: ConfigProvider): VanillaDurability {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigProvider,
    ) : VanillaDurability, SkillConditionBase(config) {

        override val durability: Evaluable<*> by config.entry<Evaluable<*>>("durability")
        override val resolver: TagResolver = TagResolver.empty()

        override fun newSession(context: SkillContext): SkillConditionSession {
            val itemStack = context.optional(SkillContextKey.ITEM_STACK) ?: return SkillConditionSession.alwaysFailure()
            val itemMeta = itemStack.itemMeta as? Damageable ?: return SkillConditionSession.alwaysFailure()
            val engine = context.get(SkillContextKey.MOCHA_ENGINE)
            val isSuccess = (itemMeta.maxDamage - itemMeta.damage) >= durability.evaluate(engine)
            return SessionImpl(isSuccess)
        }

        inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillContext) {
                notification.notifySuccess(context)
            }

            override fun onFailure(context: SkillContext) {
                notification.notifyFailure(context)
            }
        }
    }
}