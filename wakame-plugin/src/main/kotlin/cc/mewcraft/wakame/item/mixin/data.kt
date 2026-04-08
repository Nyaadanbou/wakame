package cc.mewcraft.wakame.item.mixin

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.bridge.item.ServerItemDataContainer
import cc.mewcraft.wakame.item.data.ItemDataContainer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.BOOTSTRAP)
internal object ItemDataBootstrap {
    @InitFun
    fun init() {
        ServerItemDataContainer.codec = ItemDataContainer.codec()
        LOGGER.info("Registered Codec for ${ServerItemDataContainer::class.simpleName}")
    }
}