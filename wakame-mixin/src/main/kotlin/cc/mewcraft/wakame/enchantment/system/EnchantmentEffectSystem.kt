package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.getListenerBasedEffects
import cc.mewcraft.wakame.enchantment.koishEnchantments
import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import cc.mewcraft.wakame.item.ItemStackEffectiveness
import cc.mewcraft.wakame.item.forEachChangingEntry
import org.bukkit.entity.Player

/**
 * 读取 [net.minecraft.world.item.enchantment.Enchantment.effects],
 * 然后从中创建对应的 component, 并添加到 player 上.
 */
object EnchantmentEffectSystem : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) {
        val itemSlotChanges = user.itemSlotChanges
        itemSlotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null && ItemStackEffectiveness.testSlot(slot, prev)) {
                prev.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getListenerBasedEffects().forEach { effect ->
                        effect.remove(player, level, slot)
                    }
                }
            }
            if (curr != null &&
                ItemStackEffectiveness.testSlot(slot, curr) &&
                ItemStackEffectiveness.testLevel(player, curr) &&
                ItemStackEffectiveness.testDamaged(curr)
            ) {
                curr.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getListenerBasedEffects().forEach { effect ->
                        effect.apply(player, level, slot)
                    }
                }
            }
        }
    }
}