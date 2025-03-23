package cc.mewcraft.wakame.ability2.trigger

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries2

@Init(stage = InitStage.PRE_WORLD)
internal object AbilityTriggerRegistryLoader {
    @InitFun
    fun init() {
        AbilitySequenceTrigger.RRR // 初始化静态变量
        KoishRegistries2.ABILITY_TRIGGER.freeze()
    }
}