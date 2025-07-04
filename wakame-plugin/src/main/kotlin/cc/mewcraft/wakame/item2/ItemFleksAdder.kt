package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object ItemFleksAdder : FleksAdder {
    /**
     * 用于在 Fleks 中添加物品相关的系统和组件.
     */
    fun init() {
        addToRegistrySystem("scan_item_slot_changes") { ScanItemSlotChanges } // 监听玩家背包里的物品变化
    }
}