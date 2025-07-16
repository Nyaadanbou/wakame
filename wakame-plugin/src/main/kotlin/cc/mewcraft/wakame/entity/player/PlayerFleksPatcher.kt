package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.entity.player.system.InitItemCooldownContainer
import cc.mewcraft.wakame.entity.player.system.PlayAttackSpeedAnimation
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object PlayerFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("init_item_cooldown_container") { InitItemCooldownContainer }
        addToRegistrySystem("play_attack_speed_animation") { PlayAttackSpeedAnimation }
    }
}