package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skin.ItemSkin
import org.koin.core.component.KoinComponent

object ItemSkinRegistry : KoinComponent, Initializable, BiKnot<String, ItemSkin, Short> {
    override val INSTANCES: Registry<String, ItemSkin> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Short> = SimpleBiRegistry()

    override fun onPreWorld() = loadConfiguration()
    override fun onReload() = loadConfiguration()

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        // placeholder code: read config and populate values
    }
}