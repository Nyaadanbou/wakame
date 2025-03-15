package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

// 加载自定义物品类型
@Init(stage = InitStage.PRE_WORLD)
internal object CustomKoishItemRegistryLoader {

    @InitFun
    fun init() {

    }
}

// 加载原版套皮物品类型
@Init(stage = InitStage.PRE_WORLD)
internal object ProxiedKoishItemRegistryLoader {

    @InitFun
    fun init() {

    }
}