package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.Event.Result
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

fun Action.isClickBlock() = this == Action.LEFT_CLICK_BLOCK || this == Action.RIGHT_CLICK_BLOCK

fun Action.isClickAir() = this == Action.LEFT_CLICK_AIR || this == Action.RIGHT_CLICK_AIR

fun PlayerInteractEvent.isCompletelyDenied() = useInteractedBlock() == Result.DENY && useItemInHand() == Result.DENY

val PlayerInteractEvent.handItems: Array<ItemStack>
    get() = arrayOf(player.inventory.itemInMainHand, player.inventory.itemInOffHand)

val PlayerInteractEvent.hands: Array<Pair<EquipmentSlot, ItemStack>>
    get() = arrayOf(EquipmentSlot.HAND to player.inventory.itemInMainHand, EquipmentSlot.OFF_HAND to player.inventory.itemInOffHand)

fun callEvent(event: Event) = Bukkit.getPluginManager().callEvent(event)

fun Listener.registerEvents() {
    Bukkit.getPluginManager().registerSuspendingEvents(this, NEKO_PLUGIN)
}

fun Listener.unregisterEvents() {
    HandlerList.unregisterAll(this)
}
