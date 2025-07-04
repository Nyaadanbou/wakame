package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.entity.player.system.InitItemCooldownContainer
import cc.mewcraft.wakame.entity.player.system.PlayAttackSpeedAnimation
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object PlayerFleksAdder : FleksAdder {
    /**
     * 用于在 Fleks 中添加玩家相关的系统和组件.
     */
    fun init() {
        addToRegistrySystem("init_item_cooldown_container") { InitItemCooldownContainer }
        addToRegistrySystem("play_attack_speed_animation") { PlayAttackSpeedAnimation }
    }
}