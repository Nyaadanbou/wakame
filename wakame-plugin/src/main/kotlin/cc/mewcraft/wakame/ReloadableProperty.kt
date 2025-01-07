package cc.mewcraft.wakame

import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// TODO 考虑把萌芽的 reload 逻辑分成几个部分, 每个部分可以独立重载
/**
 * A property that will be reset using [loader] upon [NekoCommandReloadEvent] being fired.
 *
 * @param T the property type
 * @property loader the loader to load the value
 * @property value the cached value
 */
class ReloadableProperty<T>(
    private val loader: () -> T,
) : ReadOnlyProperty<Any?, T>, Listener {
    private var value: T? = null

    init {
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            registerEvents()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // properties are reloaded the latest because some of them depend on configs
    private fun on(e: NekoCommandReloadEvent) {
        reload()
    }

    private fun get(): T {
        val value = this.value
        if (value == null) {
            val createdValue = loader()
            this.value = createdValue
            return createdValue
        }
        return value
    }

    private fun reload() {
        value = null
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
}