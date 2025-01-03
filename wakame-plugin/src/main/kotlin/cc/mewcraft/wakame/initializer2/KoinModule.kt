package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.util.buildYamlLoader
import org.koin.core.module.Module
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

const val MAIN_CONFIG_NODE = "main_configuration_node"

internal fun initializer2Module(): Module = module {

    factory {
        val plugin = get<WakamePlugin>()
        val configFile = plugin.getBundledFile("config.yml")
        val configText = configFile.bufferedReader().use { it.readText() }
        buildYamlLoader().buildAndLoadString(configText)
    } withOptions {
        named(MAIN_CONFIG_NODE)
    }

}