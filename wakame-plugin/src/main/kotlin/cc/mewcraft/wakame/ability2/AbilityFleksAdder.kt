package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.system.AbilityActivator
import cc.mewcraft.wakame.ability2.system.AbilityRemover
import cc.mewcraft.wakame.ability2.system.ConsumeManaForAbilities
import cc.mewcraft.wakame.ability2.system.InitAbilityContainer
import cc.mewcraft.wakame.ability2.system.InitPlayerCombo
import cc.mewcraft.wakame.ability2.system.RenderOnceOffItemName
import cc.mewcraft.wakame.ability2.system.TickAbilityBlackhole
import cc.mewcraft.wakame.ability2.system.TickAbilityBlink
import cc.mewcraft.wakame.ability2.system.TickAbilityDash
import cc.mewcraft.wakame.ability2.system.TickAbilityMultiJump
import cc.mewcraft.wakame.ecs.FleksAdder
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object AbilityFleksAdder : FleksAdder {
    @InitFun
    fun init() {
        addToRegistrySystem("ability_activator") { AbilityActivator } // “激活”玩家装备的技能
        addToRegistrySystem("ability_remover") { AbilityRemover } // “移除”玩家装备的技能
        addToRegistrySystem("consume_mana_for_abilities") { ConsumeManaForAbilities } // 消耗玩家的法力值来使用技能
        addToRegistrySystem("init_ability_container") { InitAbilityContainer } // 初始化玩家的技能容器
        addToRegistrySystem("init_player_combo") { InitPlayerCombo } // 初始化玩家的连招状态
        addToRegistrySystem("render_once_off_item_name") { RenderOnceOffItemName } // 渲染一次性物品的名称

        /* 添加技能相关的 Tick 系统 */

        addToRegistrySystem("tick_ability_blackhole") { TickAbilityBlackhole }
        addToRegistrySystem("tick_ability_blink") { TickAbilityBlink }
        addToRegistrySystem("tick_ability_dash") { TickAbilityDash }
        addToRegistrySystem("tick_ability_multi_jump") { TickAbilityMultiJump }
    }
}