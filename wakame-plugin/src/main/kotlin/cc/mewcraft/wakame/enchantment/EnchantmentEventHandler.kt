package cc.mewcraft.wakame.enchantment

import cc.mewcraft.commons.collections.mapKeysNotNull
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class EnchantmentEventHandler {
    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        if (oldItem != null) {
            val customEnchantments = oldItem.enchantments
                .mapKeysNotNull { (enchantment, _) ->
                    WakameEnchantments.get(enchantment)
                }
            for ((enchantment, level) in customEnchantments) {
                if (slot.testEquipmentSlotGroups(enchantment.equipmentSlotGroups)) {
                    val effects = enchantment.getEffects(level)
                    effects.forEach { it.remove(player.toUser()) }
                }
            }
        }
        if (newItem != null) {
            val customEnchantments = newItem.enchantments
                .mapKeysNotNull { (enchantment, _) ->
                    WakameEnchantments.get(enchantment)
                }
            for ((enchantment, level) in customEnchantments) {
                if (slot.testEquipmentSlotGroups(enchantment.equipmentSlotGroups)) {
                    val effects = enchantment.getEffects(level)
                    effects.forEach { it.apply(player.toUser()) }
                }
            }
        }
    }
}