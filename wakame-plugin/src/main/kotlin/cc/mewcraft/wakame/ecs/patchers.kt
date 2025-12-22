package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.system.RenderParticle
import cc.mewcraft.wakame.enchantment.system.*
import cc.mewcraft.wakame.entity.attribute.system.ApplyAttributeEffects
import cc.mewcraft.wakame.entity.attribute.system.InitAttributeContainer
import cc.mewcraft.wakame.entity.player.system.InitItemCooldownContainer
import cc.mewcraft.wakame.entity.player.system.PlayAttackSpeedAnimation
import cc.mewcraft.wakame.item.ScanItemSlotChanges
import cc.mewcraft.wakame.item.behavior.impl.weapon.SwitchKatana
import cc.mewcraft.wakame.item.behavior.impl.weapon.TickKatana
import cc.mewcraft.wakame.kizami.system.ApplyKizamiEffects
import cc.mewcraft.wakame.kizami.system.InitKizamiContainer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_FLEKS)
object AttributeFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("apply_attribute_effects") { ApplyAttributeEffects } // 将物品上的属性效果应用到玩家
        addToRegistrySystem("init_attribute_container") { InitAttributeContainer } // 初始化玩家的属性容器
    }
}

@Init(stage = InitStage.PRE_FLEKS)
object CommonFleksPatcher : FleksPatcher {

    @InitFun
    fun init() {
        addToRegistrySystem("render_particle") { RenderParticle } // 渲染粒子效果
    }
}

@Init(stage = InitStage.PRE_FLEKS)
object EnchantmentFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("apply_enchantment_effect") { ApplyEnchantmentEffect } // 将物品上的附魔效果应用到玩家
        addToRegistrySystem("tick_antigrav_shot_enchantment") { TickAntigravShotEnchantment }
        addToRegistrySystem("tick_attribute_enchantment") { TickAttributeEnchantment }
        addToRegistrySystem("tick_blast_mining_enchantment") { TickBlastMiningEnchantment }
        addToRegistrySystem("tick_fragile_enchantment") { TickFragileEnchantment }
        addToRegistrySystem("tick_smelter_enchantment") { TickSmelterEnchantment }
        addToRegistrySystem("tick_veinminer_enchantment") { TickVeinminerEnchantment }
    }
}

@Init(stage = InitStage.PRE_FLEKS)
object ItemBehaviorFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("switch_katana") { SwitchKatana } // 当玩家切换太刀时更新太刀状态
        addToRegistrySystem("tick_katana") { TickKatana } // 更新每 tick 的太刀状态
    }
}

@Init(stage = InitStage.PRE_FLEKS)
object ItemFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("scan_item_slot_changes") { ScanItemSlotChanges } // 监听玩家背包里的物品变化
    }
}

@Init(stage = InitStage.PRE_FLEKS)
object KizamiFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("apply_kizami_effects") { ApplyKizamiEffects } // 将物品上的铭刻效果应用到玩家
        addToRegistrySystem("init_kizami_container") { InitKizamiContainer } // 初始化玩家的铭刻容器
    }
}

@Init(stage = InitStage.PRE_FLEKS)
object PlayerFleksPatcher : FleksPatcher {
    @InitFun
    fun init() {
        addToRegistrySystem("init_item_cooldown_container") { InitItemCooldownContainer }
        addToRegistrySystem("play_attack_speed_animation") { PlayAttackSpeedAnimation }
    }
}