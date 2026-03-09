package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.getEffectList
import cc.mewcraft.wakame.enchantment.koishEnchantments
import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import cc.mewcraft.wakame.item.ItemStackEffectiveness
import cc.mewcraft.wakame.item.forEachChangingEntry
import cc.mewcraft.wakame.mixin.support.ExtraEnchantmentEffectComponents
import org.bukkit.entity.Player

/**
 * @see cc.mewcraft.wakame.enchantment.effect.EnchantmentAttributeEffect
 */
object EnchantmentAttributeSystem : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) {
        val itemSlotChanges = user.itemSlotChanges
        itemSlotChanges.forEachChangingEntry { slot, curr, prev ->
            // 返回的 slot 一定都是在当前 tick 发生了变化的,
            // 因此这个 if-block 不可能执行, 写在这里仅供参考
            if (prev == null && curr == null) {
                return
            }
            // 对于 prev (变化之前的物品), 我们需要从玩家身上 <移除> 其属性修饰器
            if (prev != null &&
                ItemStackEffectiveness.testSlot(slot, prev)
            ) {
                prev.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getEffectList(ExtraEnchantmentEffectComponents.ATTRIBUTES).forEach { attribute ->
                        attribute.remove(player, level, slot)
                    }
                }
            }
            // 对于 curr (变化之后的物品), 我们需要 <添加> 其属性修饰器到玩家身上
            if (curr != null &&
                ItemStackEffectiveness.testSlot(slot, curr) &&
                ItemStackEffectiveness.testLevel(player, curr) &&
                ItemStackEffectiveness.testDamaged(curr)
            ) {
                curr.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getEffectList(ExtraEnchantmentEffectComponents.ATTRIBUTES).forEach { attribute ->
                        attribute.apply(player, level, slot)
                    }
                }
            }
        }
    }
}