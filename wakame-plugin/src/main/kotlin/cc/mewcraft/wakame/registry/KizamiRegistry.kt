package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object KizamiRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Kizami> by RegistryBase(),
    BiMapRegistry<String, Byte> by BiMapRegistryBase() {

    // constants
    val CONFIG_LOADER_QUALIFIER = named("kizami_config_loader")

    // configuration stuff
    private val configLoader: NekoConfigurationLoader by inject(CONFIG_LOADER_QUALIFIER)
    private lateinit var configNode: NekoConfigurationNode

    private fun loadConfiguration() {
        configNode = configLoader.load()
        // TODO read config and populate values
    }

    override fun onPreWorld() {
        TODO("Not yet implemented")
    }

    override fun onReload() {
        TODO("Not yet implemented")
    }
}