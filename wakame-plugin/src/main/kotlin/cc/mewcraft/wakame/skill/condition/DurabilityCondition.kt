package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.binary.playNekoStackOrNull
import cc.mewcraft.wakame.item.getBehaviorOrNull
import cc.mewcraft.wakame.item.schema.behavior.Damageable
import cc.mewcraft.wakame.item.schema.behavior.decreaseDurabilityNaturally
import cc.mewcraft.wakame.skill.Caster

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
            val nekoStack = context.itemStack?.playNekoStackOrNull
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
            }
        }

        override fun cost(context: SkillCastContext) {
            val nekoStack = context.itemStack?.playNekoStackOrNull
            val damageable = nekoStack?.getBehaviorOrNull<Damageable>()
            if (damageable == null) {
                // TODO 原版物品失去耐久
            } else {
                nekoStack.decreaseDurabilityNaturally(requireDurability)
            }
        }

        override fun notifyFailure(context: SkillCastContext) {
            // TODO 通知玩家物品耐久度不足
            val player = context.caster as? Caster.Player ?: return
            player.bukkitPlayer.sendMessage("物品耐久度不足")
        }
    }
}