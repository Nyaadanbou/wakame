package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

object ItemSkinRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, ItemSkin> by RegistryBase(),
    BiMapRegistry<String, Short> by BiMapRegistryBase() {

    // constants
    val CONFIG_LOADER_QUALIFIER: StringQualifier = named("item_skin_config_loader")

    // configuration stuff
    private val configLoader: NekoConfigurationLoader by inject(CONFIG_LOADER_QUALIFIER)
    private lateinit var configNode: NekoConfigurationNode

    private fun loadConfiguration() {
        configNode = configLoader.load()
        // TODO read config and populate values
    }

    override fun onReload() {
        loadConfiguration()
    }

    override fun onPreWorld() {
        loadConfiguration()
    }
}