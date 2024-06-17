package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.binary.tryNekoStack
import cc.mewcraft.wakame.item.getBehaviorOrNull
import cc.mewcraft.wakame.item.schema.behavior.Damageable
import cc.mewcraft.wakame.item.schema.behavior.decreaseDurabilityNaturally
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
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
    val durability: Int

    companion object Factory : SkillConditionFactory<NekoDurability> {
        override fun create(config: ConfigProvider): NekoDurability {
            return DefaultImpl(config)
        }
    }

    private class DefaultImpl(
        config: ConfigProvider,
    ) : SkillConditionBase(config), NekoDurability {

        override val durability: Int by config.entry<Int>("durability")
        override val resolver: TagResolver = Placeholder.component(this.id, Component.text(this.durability))

        override fun newSession(context: SkillCastContext): SkillConditionSession {
            val nekoStack = context.optional(SkillCastContextKey.NEKO_STACK) ?: return SkillConditionSession.alwaysFailure()
            val damageable = nekoStack.getBehaviorOrNull<Damageable>() ?: return SkillConditionSession.alwaysFailure()
            val durabilityMeta = nekoStack.getMetaAccessor<BDurabilityMeta>()
            if (!durabilityMeta.exists) {
                return SkillConditionSession.alwaysFailure()
            }
            return SessionImpl(durability + durabilityMeta.damage() <= durabilityMeta.threshold())
        }

        private inner class SessionImpl(
            override val isSuccess: Boolean,
        ) : SkillConditionSession {
            private val notification: Notification = Notification()

            override fun onSuccess(context: SkillCastContext) {
                notification.notifySuccess(context)

                val nekoStack = context.optional(SkillCastContextKey.ITEM_STACK)?.tryNekoStack
                val damageable = nekoStack?.getBehaviorOrNull<Damageable>() ?: return
                nekoStack.decreaseDurabilityNaturally(durability)
            }

            override fun onFailure(context: SkillCastContext) {
                notification.notifyFailure(context)
            }
        }
    }
}