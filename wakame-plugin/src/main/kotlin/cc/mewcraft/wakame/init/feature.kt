package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.feature.EquipmentSlotsToKeepOnDeath
import cc.mewcraft.wakame.feature.ResetContainerOnLootGenerate
import cc.mewcraft.wakame.feature.StopBreakingLootChests
import cc.mewcraft.wakame.feature.TeleportOnJoinListener
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents

@Init(InitStage.POST_WORLD)
object FeatureInitializer {

    @InitFun
    fun init() {
        // equipment slots to keep on death
        EquipmentSlotsToKeepOnDeath().registerEvents()

        // teleport on join
        TeleportOnJoinListener().registerEvents()

        // reset container on loot generate
        ResetContainerOnLootGenerate().registerEvents()

        // stop breaking loot chests
        StopBreakingLootChests().registerEvents()
    }
}