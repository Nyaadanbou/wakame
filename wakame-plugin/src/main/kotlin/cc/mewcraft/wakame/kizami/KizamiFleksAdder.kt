package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.kizami2.system.ApplyKizamiEffects
import cc.mewcraft.wakame.kizami2.system.InitKizamiContainer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object KizamiFleksAdder : FleksAdder {
    @InitFun
    fun init() {
        addToRegistrySystem("apply_kizami_effects") { ApplyKizamiEffects } // 将物品上的铭刻效果应用到玩家
        addToRegistrySystem("init_kizami_container") { InitKizamiContainer } // 初始化玩家的铭刻容器
    }
}