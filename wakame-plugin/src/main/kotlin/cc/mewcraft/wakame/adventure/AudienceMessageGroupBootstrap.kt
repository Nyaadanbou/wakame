package cc.mewcraft.wakame.adventure

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.KOISH_NAMESPACE

@Init(InitStage.BOOTSTRAP)
internal object AudienceMessageGroupBootstrap {
    @InitFun
    fun init() {
        ConfigAccess.registerSerializer(KOISH_NAMESPACE, AudienceMessageGroup.SERIALIZER)
    }
}