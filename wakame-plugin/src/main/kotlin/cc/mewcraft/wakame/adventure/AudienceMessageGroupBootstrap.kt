package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.KOISH_NAMESPACE

@Init(stage = InitStage.PRE_CONFIG)
internal object AudienceMessageGroupBootstrap {
    @InitFun
    fun init() {
        Configs.registerSerializer(KOISH_NAMESPACE, AudienceMessageGroup.SERIALIZER)
    }
}