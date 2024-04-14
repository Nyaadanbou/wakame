package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry

/**
 * 技能释放需要物品耐久度。
 */
interface DurabilityCondition : SkillCondition {
    val requireDurability: Int

    companion object Factory : SkillConditionFactory<DurabilityCondition> {
        override fun provide(config: ConfigProvider): DurabilityCondition {
            val priority = config.optionalEntry<SkillCondition.Priority>("priority").orElse(SkillCondition.Priority.NORMAL)
            val requireDurability = config.entry<Int>("require_durability")
            return Default(priority, requireDurability)
        }
    }

    class Default(
        priority: Provider<SkillCondition.Priority>,
        requireDurability: Provider<Int>,
    ) : DurabilityCondition {
        override val priority: SkillCondition.Priority by priority
        override val requireDurability: Int by requireDurability

        override fun test(context: SkillCastContext): Boolean {
            /* val itemStack = context.itemStack FIXME 修复编译错误
            val nekoStack = PlayNekoStackFactory.maybe(itemStack)
            val damageable = nekoStack?.getBehaviorOrNull<Damageable>()
            if (damageable == null) {
                // TODO 原版物品耐久检测
                return false
            } else {
                val durabilityMeta = nekoStack.getMetaAccessor<BDurabilityMeta>()
                if (!durabilityMeta.exists) {
                    return false
                }
                return requireDurability + durabilityMeta.damage() <= durabilityMeta.threshold()
            } */
            return false
        }

        override fun cost(context: SkillCastContext) {
            /* val itemStack = context.itemStack FIXME 修复编译错误
            val nekoStack = PlayNekoStackFactory.maybe(itemStack)
            val damageable = nekoStack?.getBehaviorOrNull<Damageable>()
            if (damageable == null) {
                // TODO 原版物品失去耐久
            } else {
                nekoStack.decreaseDurabilityNaturally(requireDurability)
            } */
        }

        override fun notifyFailure(context: SkillCastContext) {
            // TODO 通知玩家物品耐久度不足
            // context.player.sendMessage("物品耐久度不足") FIXME 修复编译错误
        }
    }
}