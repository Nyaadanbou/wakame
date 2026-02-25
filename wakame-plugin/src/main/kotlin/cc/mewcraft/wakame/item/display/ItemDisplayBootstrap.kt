package cc.mewcraft.wakame.item.display

import cc.mewcraft.wakame.item.display.implementation.standard.StandardItemRenderer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        StandardItemRenderer::class
    ]
)
object ItemDisplayBootstrap {

    @InitFun
    fun init() {
        ItemStackRenderer.register(StandardItemRenderer)
    }
}