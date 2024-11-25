package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.Bukkit
import org.bukkit.event.Event.Result
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

//<editor-fold desc="Event Handlers">
fun Action.isClickBlock() = this == Action.LEFT_CLICK_BLOCK || this == Action.RIGHT_CLICK_BLOCK

fun Action.isClickAir() = this == Action.LEFT_CLICK_AIR || this == Action.RIGHT_CLICK_AIR

fun PlayerInteractEvent.isCompletelyDenied() = useInteractedBlock() == Result.DENY && useItemInHand() == Result.DENY
//</editor-fold>

//<editor-fold desc="Event Registrations">
fun Listener.registerEvents() {
    Bukkit.getPluginManager().registerEvents(this, NEKO)
}

fun Listener.registerSuspendingEvents() {
    Bukkit.getPluginManager().registerSuspendingEvents(this, NEKO)
}

fun Listener.unregisterEvents() {
    HandlerList.unregisterAll(this)
}
//</editor-fold>
