package cc.mewcraft.wakame

import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object PluginProvider : KoinComponent {
    private val plugin: WakamePlugin by inject()

    fun <T : Plugin> get(): T {
        @Suppress("UNCHECKED_CAST")
        return plugin as T
    }

    fun <T : Plugin> lazy(): Lazy<T> {
        return lazy { get() }
    }
}