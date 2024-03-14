package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object RarityRegistry : KoinComponent, Initializable,
    Registry<String, Rarity> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    /**
     * The default rarity. By design, it should be the most common rarity.
     */
    val DEFAULT: Rarity by lazy { values.first() }

    // configuration stuff
    private lateinit var root: NekoConfigurationNode

    private fun loadConfiguration() {
        clearBoth()

        root = get<NekoConfigurationLoader>(named(RARITY_CONFIG_LOADER)).load()

        root.node("rarities").childrenMap().forEach { (_, n) ->
            val rarity = n.requireKt<Rarity>()
            registerBothMapping(rarity.key, rarity)
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}