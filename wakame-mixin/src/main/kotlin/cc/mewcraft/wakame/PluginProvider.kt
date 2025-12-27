package cc.mewcraft.wakame

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus

object PluginProvider {

    /**
     * (Koish) 插件实例.
     */
    @JvmStatic
    private lateinit var instance: JavaPlugin

    @ApiStatus.Internal
    fun set(plugin: JavaPlugin) {
        this.instance = plugin
    }

    fun get(): JavaPlugin {
        return instance
    }
}