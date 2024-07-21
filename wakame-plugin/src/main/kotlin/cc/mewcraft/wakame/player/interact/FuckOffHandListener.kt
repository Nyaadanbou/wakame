package cc.mewcraft.wakame.player.interact

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 副手射箭会引发一系列问题，包括但不限于：
 * 伤害不对
 * 存在交互优先级问题
 * 经过几个小时的讨论，得出必须要枪毙副手射箭
 */
class FuckOffHandListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun on(event: EntityShootBowEvent) {
        val livingEntity = event.entity
        if (livingEntity !is Player) return
        if (event.hand != EquipmentSlot.OFF_HAND) return
        val bow = event.bow ?: return
        if (bow.type != Material.BOW && bow.type != Material.CROSSBOW) return

        event.isCancelled = true
        val playerInventory = livingEntity.inventory
        val itemInMainHand = playerInventory.itemInMainHand
        playerInventory.setItem(EquipmentSlot.HAND, bow)
        playerInventory.setItem(EquipmentSlot.OFF_HAND, itemInMainHand)
    }
}