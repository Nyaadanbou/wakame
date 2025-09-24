package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.system.*
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
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
        addToRegistrySystem("consume_mana_for_abilities") { ConsumeManaForAbilities } // 消耗玩家的法力值来使用技能
        addToRegistrySystem("count_tick") { CountTick } // 记录 entity 存在的 tick 数
        if (MAIN_CONFIG.entry<ManaDisplayType>("display_mana_provider").get() == ManaDisplayType.KOISH) {
            addToRegistrySystem("display_mana") { DisplayMana } // 显示玩家的魔法值
        }
        addToRegistrySystem("init_ability_container") { InitAbilityContainer } // 初始化玩家的技能容器
        addToRegistrySystem("init_mana") { InitMana } // 初始化玩家的魔法值
        addToRegistrySystem("init_player_combo") { InitPlayerCombo } // 初始化玩家的连招状态
        addToRegistrySystem("render_once_off_item_name") { RenderOnceOffItemName } // 渲染一次性物品的名称
        addToRegistrySystem("restore_mana") { RestoreMana } // 恢复玩家的魔法值
        addToRegistrySystem("update_max_mana") { UpdateMaxMana } // 更新玩家的最大魔法值

        /* 添加技能相关的 Tick 系统 */

        addToRegistrySystem("tick_ability_blackhole") { TickAbilityBlackhole }
        addToRegistrySystem("tick_ability_blink") { TickAbilityBlink }
        addToRegistrySystem("tick_ability_dash") { TickAbilityDash }
        addToRegistrySystem("tick_ability_multi_jump") { TickAbilityMultiJump }
    }
}