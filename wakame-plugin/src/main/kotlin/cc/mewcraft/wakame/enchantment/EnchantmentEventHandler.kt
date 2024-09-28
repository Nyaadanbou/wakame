package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 处理魔咒相关事件的处理器.
 *
 * 在 1.20.5 以前
 */
internal class EnchantmentEventHandler {

    /**
     * 处理玩家装备栏物品变更事件.
     */
    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        val user = player.toUser()
        if (oldItem != null) {
            val customEnchantments = oldItem.customEnchantments
            for ((enchantment, level) in customEnchantments) {
                if (slot.testEquipmentSlotGroups(enchantment.handle.activeSlotGroups)) {
                    for (effect in enchantment.getEffects(level, slot)) {
                        effect.removeFrom(user)
                    }
                }
            }
        }
        if (newItem != null) {
            val customEnchantments = newItem.customEnchantments
            for ((enchantment, level) in customEnchantments) {
                if (slot.testEquipmentSlotGroups(enchantment.handle.activeSlotGroups)) {
                    for (effect in enchantment.getEffects(level, slot)) {
                        effect.applyTo(user)
                    }
                }
            }
        }
    }
}