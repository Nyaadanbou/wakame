package cc.mewcraft.wakame.player.interact

import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.runTaskTimer
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

// FIXME 该类实现很丑，尽量重写后再使用

/**
 * A class wrapping Bukkit's [PlayerInteractEvent], which will not be called for the
 * other hand if an action has been performed (marked via [WrappedPlayerInteractEvent.actionPerformed]).
 */
class WrappedPlayerInteractEvent(val event: PlayerInteractEvent) : Event() {

    /**
     * Whether an action has been performed (by Nova or addons).
     *
     * Possible actions might be: Nova block placed, Gui opened, custom armor equipped, etc.
     * Note that this does not include possible vanilla actions that might happen if the [PlayerInteractEvent] is not cancelled.
     *
     * If this is set to true, possible subsequent offhand events will not be fired.
     */
    var actionPerformed = false

    companion object : Listener {

        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers

        private val performedCustomInteractions = HashSet<Pair<Player, Action>>()

        init {
            registerEvents()
            runTaskTimer(0, 1) { performedCustomInteractions.clear() }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun handleInteract(event: PlayerInteractEvent) {
            val playerAction = event.player to event.action
            if (playerAction in performedCustomInteractions) {
                event.isCancelled = true
            } else {
                val wrappedEvent = WrappedPlayerInteractEvent(event)
                wrappedEvent.callEvent()
                if (wrappedEvent.actionPerformed)
                    performedCustomInteractions += playerAction
            }
        }

    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }

}