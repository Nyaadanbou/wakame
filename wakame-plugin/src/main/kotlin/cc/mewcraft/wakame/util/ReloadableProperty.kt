package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.eventbus.ConfigurationReloadEvent
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A property that will be reset using [loader] upon [ConfigurationReloadEvent] being fired.
 *
 * @param T the property type
 * @property loader the loader to load the value
 * @property value the cached value
 */
class ReloadableProperty<T>(
    private val loader: () -> T,
) : ReadOnlyProperty<Any?, T> {

    init {
        MapEventBus.subscribe<ConfigurationReloadEvent> { reload() }
    }

    private var value: T? = null

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