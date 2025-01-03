package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.skin.ItemSkin
import org.koin.core.component.KoinComponent

@Init(
    stage = InitStage.PRE_WORLD,
)
object ItemSkinRegistry : KoinComponent, BiKnot<String, ItemSkin, Short> {
    override val INSTANCES: Registry<String, ItemSkin> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Short> = SimpleBiRegistry()

    @InitFun
    private fun onPreWorld() = loadConfiguration()
//    override fun onReload() = loadConfiguration()

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        // placeholder code: read config and populate values
    }
}