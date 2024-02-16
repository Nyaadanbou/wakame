package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object ItemSkinRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, ItemSkin> by HashMapRegistry(),
    BiMapRegistry<String, Short> by HashBiMapRegistry() {

    // configuration stuff
    private lateinit var root: NekoConfigurationNode

    private fun loadConfiguration() {
        @OptIn(InternalApi::class) clearBoth()

        root = get<NekoConfigurationLoader>(named(SKIN_CONFIG_LOADER)).load()

        // TODO read config and populate values
    }

    override fun onReload() {
        loadConfiguration()
    }

    override fun onPreWorld() {
        loadConfiguration()
    }
}