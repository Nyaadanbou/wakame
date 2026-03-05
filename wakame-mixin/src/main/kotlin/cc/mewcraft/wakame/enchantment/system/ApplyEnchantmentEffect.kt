package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.getListenerBasedEffects
import cc.mewcraft.wakame.enchantment.koishEnchantments
import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.ItemStackEffectiveness
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * 读取 [net.minecraft.world.item.enchantment.Enchantment.effects],
 * 然后从中创建对应的 component, 并添加到 player 上.
 */
object ApplyEnchantmentEffect : Listener {

    @EventHandler
    private fun on(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val curr = event.newItemStack
        val prev = event.oldItemStack
        val slot = event.slot
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