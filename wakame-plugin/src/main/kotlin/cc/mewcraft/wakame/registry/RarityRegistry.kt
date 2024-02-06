package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.typedRequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object RarityRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Rarity> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(RARITY_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    private fun loadConfiguration() {
        node = loader.load()
        node.childrenList().forEach { n ->
            val rarity = n.typedRequire<Rarity>()
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