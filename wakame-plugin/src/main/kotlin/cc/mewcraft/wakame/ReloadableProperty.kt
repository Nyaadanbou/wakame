package cc.mewcraft.wakame

import cc.mewcraft.wakame.event.NekoReloadEvent
import cc.mewcraft.wakame.util.TestEnvironment
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

fun <T> reloadable(loader: () -> T): ReloadableProperty<T> = ReloadableProperty(loader)

class ReloadableProperty<T>(
    private val loader: () -> T,
) : ReadOnlyProperty<Any?, T>, Listener {
    private var value: T? = null

    // for debug purpose
    private var property: String? = null
    private var declaringClass: String? = null

    init {
        if (!TestEnvironment.isRunningJUnit()) {
            registerEvents()
        }
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
        println("[ReloadableProperty] '${declaringClass}.${property}' has been reloaded")
        value = null
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (this.property == null) {
            this.property = property.name
        }
        if (this.declaringClass == null) {
            this.declaringClass = property.javaField?.declaringClass?.name
        }
        return get()
    }
}