package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object ItemSkinRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, ItemSkin> by HashMapRegistry(),
    BiMapRegistry<String, Short> by HashBiMapRegistry() {

    // empty skin - TEST ONLY
    val EMPTY_SKIN = ItemSkin("empty", 0, "Empty")

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(SKIN_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    private fun loadConfiguration() {
        node = loader.load()
        // TODO read config and populate values
    }

    override fun onReload() {
        loadConfiguration()
    }

    override fun onPreWorld() {
        loadConfiguration()
    }
}