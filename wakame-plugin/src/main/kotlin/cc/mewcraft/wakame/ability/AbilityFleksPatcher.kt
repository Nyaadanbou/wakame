package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.system.*
import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object AbilityFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("ability_activator") { AbilityActivator } // “激活”玩家装备的技能
        addToRegistrySystem("ability_remover") { AbilityRemover } // “移除”玩家装备的技能
        addToRegistrySystem("count_tick") { CountTick } // 记录 entity 存在的 tick 数
        addToRegistrySystem("init_ability_container") { InitAbilityContainer } // 初始化玩家的技能容器
        addToRegistrySystem("init_player_combo") { InitPlayerCombo } // 初始化玩家的连招状态

        /* 添加技能相关的 Tick 系统 */

        addToRegistrySystem("tick_ability_blackhole") { TickAbilityBlackhole }
        addToRegistrySystem("tick_ability_blink") { TickAbilityBlink }
        addToRegistrySystem("tick_ability_dash") { TickAbilityDash }
        addToRegistrySystem("tick_ability_multi_jump") { TickAbilityMultiJump }
    }
}