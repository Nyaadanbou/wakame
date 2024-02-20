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
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.File

const val PLUGIN_DATA_DIR = "plugin_data_dir"
const val MINIMESSAGE_FULL = "minimessage_full"

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

    ////// ComponentSerializer injections

    single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
    single<MiniMessage>(named(MINIMESSAGE_FULL)) { MiniMessage.miniMessage() }
}