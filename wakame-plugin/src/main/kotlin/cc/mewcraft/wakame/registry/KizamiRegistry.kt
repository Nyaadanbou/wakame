package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object KizamiRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Kizami> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    // configuration stuff
    private lateinit var root: NekoConfigurationNode

    @OptIn(InternalApi::class)
    private fun loadConfiguration() {
        clearBoth()

        root = get<NekoConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)).load()

        root.node("kizami").childrenMap().forEach { (_, n) ->
            val kizami = n.requireKt<Kizami>()
            registerBothMapping(kizami.name, kizami)
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}