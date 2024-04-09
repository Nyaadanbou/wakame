package cc.mewcraft.wakame

import cc.mewcraft.wakame.event.NekoReloadEvent
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A property that will be reset using [loader] upon [NekoReloadEvent] being fired.
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
        RunningEnvironment.PRODUCTION.run { registerEvents() }
    }

    @EventHandler
    private fun onNekoReload(e: NekoReloadEvent) {
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