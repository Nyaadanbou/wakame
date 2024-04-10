package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.getBehaviorOrNull
import cc.mewcraft.wakame.item.schema.behavior.Damageable
import cc.mewcraft.wakame.item.schema.behavior.decreaseDurabilityNaturally
import org.bukkit.inventory.ItemStack


/**
 * 技能释放需要物品耐久度。
 */
class DurabilityCondition(
    val itemStack: ItemStack,
    val requireDurability: Int,
) : Condition {
    override fun test(): Boolean {
        val nekoStack = PlayNekoStackFactory.maybe(itemStack)
        val damageable = nekoStack?.getBehaviorOrNull<Damageable>()
        if (damageable == null) {
            TODO("原版物品耐久检测")
        } else {
            val durabilityMeta = nekoStack.getMetaAccessor<BDurabilityMeta>()
            if (!durabilityMeta.exists) {
                return false
            }
            return requireDurability + durabilityMeta.damage() <= durabilityMeta.threshold()
        }
    }

    override fun cost() {
        val nekoStack = PlayNekoStackFactory.maybe(itemStack)
        val damageable = nekoStack?.getBehaviorOrNull<Damageable>()
        if (damageable == null) {
            TODO("原版物品失去耐久")
        } else {
            nekoStack.decreaseDurabilityNaturally(requireDurability)
        }
    }
}