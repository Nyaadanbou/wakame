package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.ItemTypeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

/**
 * 初始化 [ItemXFactoryRegistry] 的逻辑.
 */
@Init(
    stage = InitStage.PRE_WORLD, runAfter = [
        ItemTypeRegistryLoader::class]
)
object ItemXBootstrap {

    @InitFun
    fun init() {
        ItemXFactoryRegistry.register("wakame", ItemXFactoryNeko)
        ItemXFactoryRegistry.register("minecraft", ItemXFactoryVanilla)
    }
}