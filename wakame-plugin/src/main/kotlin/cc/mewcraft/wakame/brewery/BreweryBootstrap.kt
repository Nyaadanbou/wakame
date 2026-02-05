package cc.mewcraft.wakame.brewery

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.registerSerializer
import cc.mewcraft.wakame.item.SlotDisplayLoreData
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.KOISH_NAMESPACE

@Init(InitStage.BOOTSTRAP)
object BreweryBootstrap {

    @InitFun
    fun init() {
        ConfigAccess.registerSerializer(KOISH_NAMESPACE, SlotDisplayLoreData.SERIALIZER)
    }
}