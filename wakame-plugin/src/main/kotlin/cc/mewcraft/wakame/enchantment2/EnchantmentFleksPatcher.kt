package cc.mewcraft.wakame.enchantment2

import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.enchantment2.system.ApplyEnchantmentEffect
import cc.mewcraft.wakame.enchantment2.system.TickAntigravShotEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickAttributeEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickBlastMiningEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickFragileEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickSmelterEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickVeinminerEnchantment
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

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