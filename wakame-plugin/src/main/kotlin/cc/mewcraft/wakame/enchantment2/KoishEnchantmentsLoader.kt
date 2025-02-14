package cc.mewcraft.wakame.enchantment2

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.PRE_WORLD)
internal object KoishEnchantmentsLoader {

    @InitFun
    fun init() = Unit

}