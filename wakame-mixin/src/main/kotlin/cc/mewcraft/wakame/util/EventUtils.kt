package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.KoishHolder
import cc.mewcraft.wakame.SERVER
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.Event.Result
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

//<editor-fold desc="Event Handlers">
fun Action.isClickBlock() = this == Action.LEFT_CLICK_BLOCK || this == Action.RIGHT_CLICK_BLOCK

fun Action.isClickAir() = this == Action.LEFT_CLICK_AIR || this == Action.RIGHT_CLICK_AIR

fun PlayerInteractEvent.isCompletelyDenied() = useInteractedBlock() == Result.DENY && useItemInHand() == Result.DENY
//</editor-fold>

@DslMarker
annotation class ListenerRegistrationMarker

//<editor-fold desc="Event Registrations">
/**
 * Creates a Bukkit event listener and registers it to listen for a specific type of event.
 *
 * This function simplifies the process of creating and registering a Bukkit event listener.
 *
 * @param T The type of event to listen for, specified as a reified type parameter.
 * @param priority The priority at which the listener should be called, default is EventPriority.NORMAL.
 * @param ignoreCancelled Whether to ignore if the event was cancelled or not, default is false.
 * @param callback A lambda function to be executed when the specified event occurs.
 * @return A [KoishListener] instance, which can be used to later unregister the listener.
 */
@ListenerRegistrationMarker
inline fun <reified T : Event> event(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline callback: (T) -> Unit,
): KoishListener = event(T::class.java, priority, ignoreCancelled, callback)

/**
 * Creates a Bukkit event listener and registers it to listen for a specific type of event.
 *
 * This function simplifies the process of creating and registering a Bukkit event listener.
 *
 * @param T The type of event to listen for, specified as a reified type parameter.
 * @param eventClazz The class of the event to listen for.
 * @param priority The priority at which the listener should be called, default is EventPriority.NORMAL.
 * @param ignoreCancelled Whether to ignore if the event was cancelled or not, default is false.
 * @param callback A lambda function to be executed when the specified event occurs.
 * @return A [KoishListener] instance, which can be used to later unregister the listener.
 */
@ListenerRegistrationMarker
fun <T : Event> event(
    eventClazz: Class<T>,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    callback: (T) -> Unit,
): KoishListener = KoishListener().apply {
    SERVER.pluginManager.registerEvent(
        eventClazz,
        this,
        priority,
        { _, event -> if (eventClazz.isInstance(event)) callback(event as T) },
        KoishHolder.INSTANCE,
        ignoreCancelled
    )
}

/**
 * Layer on top of Bukkit's Listener to allow for easy unregistering. Returned by the above function.
 */
class KoishListener : Listener {
    fun unregister() = HandlerList.unregisterAll(this)
}

fun Listener.registerEvents() {
    Bukkit.getPluginManager().registerEvents(this, KoishHolder.INSTANCE)
}

fun Listener.unregisterEvents() {
    HandlerList.unregisterAll(this)
}
//</editor-fold>
