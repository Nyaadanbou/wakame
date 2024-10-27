package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot

/**
 * 原版弩攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: crossbow
 * ```
 */
class CrossbowShoot : AttackType {
    companion object {
        const val NAME = "crossbow"
    }

    // 禁止副手使用弩
    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        if (event.hand != EquipmentSlot.OFF_HAND) return

        val playerInventory = player.inventory
        event.setUseItemInHand(Event.Result.DENY)
        val itemInOffHand = playerInventory.itemInOffHand
        val itemInMainHand = playerInventory.itemInMainHand
        playerInventory.setItem(EquipmentSlot.HAND, itemInOffHand)
        playerInventory.setItem(EquipmentSlot.OFF_HAND, itemInMainHand)
    }
}