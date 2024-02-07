package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object KizamiRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Kizami> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(KIZAMI_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    @OptIn(InternalApi::class)
    private fun loadConfiguration() {
        clearBoth()

        node = loader.load()
        // TODO read config and populate values
    }

    override fun onPreWorld() {
        // TODO("Not yet implemented")
    }

    override fun onReload() {
        // TODO("Not yet implemented")
    }
}