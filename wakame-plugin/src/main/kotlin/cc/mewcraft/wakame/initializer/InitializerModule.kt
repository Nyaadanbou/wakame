package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.util.buildYamlConfigurationLoader
import org.koin.core.module.Module
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

const val MAIN_CONFIG_NODE = "main_configuration_node"

internal fun initializerModule(): Module = module {

    factory {
        val plugin = get<WakamePlugin>()
        val configFile = plugin.getBundledFile("config.yml")
        val configText = configFile.bufferedReader().use { it.readText() }
        buildYamlConfigurationLoader().buildAndLoadString(configText)
    } withOptions {
        named(MAIN_CONFIG_NODE)
    }

}