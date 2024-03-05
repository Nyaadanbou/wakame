package cc.mewcraft.wakame

import me.lucko.helper.plugin.KExtendedJavaPlugin
import me.lucko.helper.plugin.KHelperPlugin
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.slf4j.Logger
import java.io.File

const val PLUGIN_DATA_DIR = "plugin_data_dir"

const val PLUGIN_ASSETS_DIR = "assets"

fun wakameModule(plugin: WakamePlugin): Module = module {

    ////// Plugin injections

    single<WakamePlugin> {
        plugin
    } binds arrayOf(
        Plugin::class, JavaPlugin::class, KHelperPlugin::class, KExtendedJavaPlugin::class
    ) withOptions {
        createdAtStart()
    }
    single<PluginManager> { plugin.server.pluginManager }
    single<ComponentLogger> { plugin.componentLogger } bind Logger::class
    single<Server> { plugin.server }
    single<File>(named(PLUGIN_DATA_DIR)) { plugin.dataFolder }
    single<File>(named(PLUGIN_ASSETS_DIR)) { get<File>(named(PLUGIN_DATA_DIR)).resolve("assets") }

    ////// ComponentSerializer injections

    single<MiniMessage> { MiniMessage.miniMessage() }
    single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
}