package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.system.RenderParticle
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object CommonFleksPatcher : FleksPatcher {

    @InitFun
    fun init() {
        addToRegistrySystem("render_particle") { RenderParticle } // 渲染粒子效果
    }
}