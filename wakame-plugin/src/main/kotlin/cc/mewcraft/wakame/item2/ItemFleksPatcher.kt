package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object ItemFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("scan_item_slot_changes") { ScanItemSlotChanges } // 监听玩家背包里的物品变化
    }
}