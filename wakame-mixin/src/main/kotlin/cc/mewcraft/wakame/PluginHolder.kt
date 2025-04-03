package cc.mewcraft.wakame

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus

object PluginHolder {

    /**
     * 插件实例.
     */
    @get:JvmName("getInstance")
    @get:JvmStatic
    lateinit var INSTANCE: JavaPlugin
        private set

    @ApiStatus.Internal
    fun register(plugin: JavaPlugin) {
        this.INSTANCE = plugin
    }

}