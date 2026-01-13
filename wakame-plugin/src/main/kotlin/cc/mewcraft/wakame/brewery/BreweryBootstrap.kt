package cc.mewcraft.wakame.brewery

import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.registerSerializer
import cc.mewcraft.wakame.item.SlotDisplayLoreData
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.KOISH_NAMESPACE

@Init(InitStage.PRE_CONFIG)
object BreweryBootstrap {

    @InitFun
    fun init() {
        ConfigAccess.INSTANCE.registerSerializer(KOISH_NAMESPACE, SlotDisplayLoreData.SERIALIZER)
    }
}