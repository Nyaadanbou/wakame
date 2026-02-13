package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.catalog.OpenCatalogImpl
import cc.mewcraft.wakame.item.CustomItemRegistryLoader
import cc.mewcraft.wakame.item.ItemProxyRegistryLoader
import cc.mewcraft.wakame.item.KoishTagManager
import cc.mewcraft.wakame.item.behavior.impl.OpenCatalog
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage


// TODO 迁移逻辑

@Init(InitStage.PRE_WORLD)
object ItemInitializer {

    @InitFun
    fun init() {
        // 注册物品行为实现
        OpenCatalog.setImplementation(OpenCatalogImpl)
    }

    fun reload() {
        CustomItemRegistryLoader.reload()
        ItemProxyRegistryLoader.reload()
    }
}

@Init(InitStage.POST_WORLD)
object ItemTagInitializer {

    @InitFun
    fun init() {
        KoishTagManager.loadTags()
    }
}