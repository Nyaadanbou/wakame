package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.bridge.ExtraEnchantmentEffectsRegistrar
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(stage = InitStage.BOOTSTRAP)
internal object ExtraEnchantmentEffectsBootstrap {
    @InitFun
    fun init() {
        ExtraEnchantmentEffectsRegistrar.setImplementation(ExtraEnchantmentEffectsRegistrarImpl)
    }
}

private object ExtraEnchantmentEffectsRegistrarImpl : ExtraEnchantmentEffectsRegistrar {
    override fun bootstrap() {
        ExtraEnchantmentEffects.bootstrap()
    }
}