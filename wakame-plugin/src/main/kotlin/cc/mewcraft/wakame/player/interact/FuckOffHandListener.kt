package cc.mewcraft.wakame.player.interact

import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 副手射箭会引发一系列问题, 包括但不限于:
 * 伤害不对.
 * 存在交互优先级问题.
 * 经过几个小时的讨论, 得出必须要枪毙副手射箭
 */
class FuckOffHandListener : Listener {
    private val fuckOffHandSet = setOf(
        Material.BOW,
        Material.CROSSBOW,
        Material.TRIDENT
    )

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(event: PlayerInteractEvent) {
        val player = event.player
        if (event.hand != EquipmentSlot.OFF_HAND) return
        val playerInventory = player.inventory
        val itemInOffHand = playerInventory.itemInOffHand
        if (!fuckOffHandSet.contains(itemInOffHand.type)) return

        event.setUseItemInHand(Event.Result.DENY)
        val itemInMainHand = playerInventory.itemInMainHand
        playerInventory.setItem(EquipmentSlot.HAND, itemInOffHand)
        playerInventory.setItem(EquipmentSlot.OFF_HAND, itemInMainHand)
    }
}