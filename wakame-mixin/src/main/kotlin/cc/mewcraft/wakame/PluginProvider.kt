package cc.mewcraft.wakame

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus

object PluginProvider {

    /**
     * (Koish) 插件实例.
     */
    @get:JvmName("get")
    @get:JvmStatic
    lateinit var instance: JavaPlugin
        private set

    @ApiStatus.Internal
    fun register(plugin: JavaPlugin) {
        this.instance = plugin
    }
}