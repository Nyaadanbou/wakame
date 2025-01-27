package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

/**
 * 负责初始化伤害系统 API 的一些实例.
 */
@Init(
    stage = InitStage.PRE_WORLD
)
internal object AttributeBootstrap {

    @InitFun
    private fun init() {

        // 注册 AttributeMapAccess
        AttributeMapAccess.register(DefaultAttributeMapAccess)

        // 注册 AttributeProvider
        AttributeProvider.register(Attributes)

    }
}