package cc.mewcraft.wakame

import cc.mewcraft.wakame.event.NekoReloadEvent
import cc.mewcraft.wakame.util.listen
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Reloadable {
    fun onReload()
}

fun <T> reloadable(loader: () -> T) = ReloadableProperty(loader)
class ReloadableProperty<T>(private val loader: () -> T) : ReadOnlyProperty<Any?, T>, KoinComponent {
    private var value: T? = null

    init {
        get<WakamePlugin>().listen<NekoReloadEvent> { reload() }
    }

    fun get(): T {
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

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }
}