package cc.mewcraft.wakame

import cc.mewcraft.wakame.event.NekoReloadEvent
import cc.mewcraft.wakame.util.listen
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> reloadable(loader: () -> T) = ReloadableProperty(loader)

class ReloadableProperty<T>(private val loader: () -> T) : ReadOnlyProperty<Any?, T> {
    private var value: T? = null

    init {
        if (runCatching { Class.forName("net.minecraft.server.MinecraftServer") }.isSuccess) {
            // only register listener if we are in a real server environment
            NEKO_PLUGIN.listen<NekoReloadEvent> { reload() }
        }
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