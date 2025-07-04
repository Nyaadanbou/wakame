package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.item2.behavior.impl.weapon.SwitchKatana
import cc.mewcraft.wakame.item2.behavior.impl.weapon.TickKatana
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object ItemBehaviorFleksAdder : FleksAdder {
    @InitFun
    fun init() {
        addToRegistrySystem("switch_katana") { SwitchKatana } // 当玩家切换太刀时更新太刀状态
        addToRegistrySystem("tick_katana") { TickKatana } // 更新每 tick 的太刀状态
    }
}