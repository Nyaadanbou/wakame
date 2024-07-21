package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.inventory.meta.Damageable
import org.spongepowered.configurate.ConfigurationNode

/**
 * 检查原版物品的耐久度.
 */
interface VanillaDurability : SkillCondition {

    /**
     * 剩余耐久度大于该值, 则条件满足.
     */
    val durability: Evaluable<*>

    companion object : SkillConditionFactory<VanillaDurability> {
        override fun create(config: ConfigurationNode): VanillaDurability {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigurationNode,
    ) : VanillaDurability, SkillConditionBase(config) {

        override val durability: Evaluable<*> = config.node("durability").krequire()
        override val resolver: TagResolver = TagResolver.empty()

        override fun newSession(context: SkillContext): SkillConditionSession {
            val itemStack = context[SkillContextKey.ITEM_STACK] ?: return SkillConditionSession.alwaysFailure()
            val itemMeta = itemStack.itemMeta as? Damageable ?: return SkillConditionSession.alwaysFailure()
            val engine = context.getOrThrow(SkillContextKey.MOCHA_ENGINE)
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