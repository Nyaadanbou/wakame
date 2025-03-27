package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.attackSpeed
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot

/**
 * 原版弓攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: bow
 * ```
 */
class BowAttack : AttackType {
    companion object {
        const val NAME = "bow"
    }

    override fun handleInteract(player: Player, itemstack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (action.isRightClick) {
            if (player.attackSpeed.isActive(itemstack.id)) {
                wrappedEvent.event.setUseItemInHand(Event.Result.DENY)
            } else {
                // 禁止副手使用弓
                val event = wrappedEvent.event
                if (event.hand == EquipmentSlot.OFF_HAND) {
                    val playerInventory = player.inventory
                    event.setUseItemInHand(Event.Result.DENY)
                    val itemInOffHand = playerInventory.itemInOffHand
                    val itemInMainHand = playerInventory.itemInMainHand
                    playerInventory.setItem(EquipmentSlot.HAND, itemInOffHand)
                    playerInventory.setItem(EquipmentSlot.OFF_HAND, itemInMainHand)
                }
            }
        }

        wrappedEvent.actionPerformed = true
    }
}