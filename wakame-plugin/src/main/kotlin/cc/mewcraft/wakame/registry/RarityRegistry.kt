package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.require
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

object RarityRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Rarity> by RegistryBase(),
    BiMapRegistry<String, Byte> by BiMapRegistryBase() {

    // constants
    val CONFIG_LOADER_QUALIFIER: StringQualifier = named("rarity_config_loader")

    // configuration stuff
    private val configLoader: NekoConfigurationLoader by inject(CONFIG_LOADER_QUALIFIER)
    private lateinit var configNode: NekoConfigurationNode

    private fun loadConfiguration() {
        configNode = configLoader.load()
        configNode.childrenList().forEach { n ->
            val rarity = n.require<Rarity>()
            registerBothMapping(rarity.name, rarity)
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}