package cc.mewcraft.wakame

import me.lucko.helper.plugin.KExtendedJavaPlugin
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.slf4j.Logger

val wakameModule: Module = module {
    single<Plugin> { requireNotNull(Bukkit.getPluginManager().getPlugin("Wakame")) } binds arrayOf(
        JavaPlugin::class, KExtendedJavaPlugin::class, WakamePlugin::class
    ) withOptions {
        createdAtStart()
    }
    single<PluginManager> { Bukkit.getPluginManager() }
    single<Logger> { get<WakamePlugin>().componentLogger } bind Logger::class
    single<Server> { Bukkit.getServer() }
}