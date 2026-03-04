package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.enchantment.system.*
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents

// TODO 迁移逻辑

@Init(InitStage.POST_WORLD)
object EnchantmentInitializer {

    @InitFun
    fun init() {
        ApplyEnchantmentEffect.registerEvents()
        TickAntigravShotEnchantment.registerEvents()
        TickAttributeEnchantment.registerEvents()
        TickBlastMiningEnchantment.registerEvents()
        TickFragileEnchantment.registerEvents()
        TickSmelterEnchantment.registerEvents()
        TickVeinminerEnchantment.registerEvents()
        TickVoidEscapeEnchantment.registerEvents()
    }
}