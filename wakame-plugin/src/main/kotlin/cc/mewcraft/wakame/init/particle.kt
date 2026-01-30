package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.particle.ParticleManager
import cc.mewcraft.wakame.util.registerEvents

@Init(InitStage.POST_WORLD)
object ParticleManagerInitializer {

    @InitFun
    fun init() {
        ParticleManager.registerEvents()
    }
}