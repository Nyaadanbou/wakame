package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object RarityRegistry : KoinComponent, Initializable, BiKnot<String, Rarity, Byte> {
    override val INSTANCES: Registry<String, Rarity> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Byte> = SimpleBiRegistry()

    override fun onPreWorld() = loadConfiguration()
    override fun onReload() = loadConfiguration()

    /**
     * The default rarity. By design, it should be the most common rarity.
     */
    val DEFAULT: Rarity by lazy { INSTANCES.objects.first() }

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        val root = get<NekoConfigurationLoader>(named(RARITY_GLOBAL_CONFIG_LOADER)).load()
        root.node("rarities").childrenMap().forEach { (_, n) ->
            val rarity = n.krequire<Rarity>()
            INSTANCES.register(rarity.uniqueId, rarity)
            BI_LOOKUP.register(rarity.uniqueId, rarity.binaryId)
        }
    }
}