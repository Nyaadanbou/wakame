package cc.mewcraft.wakame.ability.trigger

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries

@Init(stage = InitStage.PRE_WORLD)
internal object TriggerRegistryLoader {
    @InitFun
    fun init() {
        AbilitySequenceTrigger.RRR // 初始化静态变量
        KoishRegistries.ABILITY_TRIGGER.freeze()
    }
}