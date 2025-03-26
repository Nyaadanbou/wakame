package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

/**
 * 负责初始化伤害系统的一些内部状态, 如 API 实例.
 */
@Init(stage = InitStage.PRE_WORLD)
internal object DamageApiBootstrap {

    @InitFun
    fun init() {
        // 注册 DamageBundleFactory
        DamageBundleFactory.register(DefaultDamageBundleFactory)

        // 注册 DamageTagsFactory
        DamageTagsFactory.register(DefaultDamageTagsFactory)

        // 注册 DamageApplier
        DamageApplier.register(BukkitDamageApplier)

        // 注册 DamageManagerApi
        DamageManagerApi.register(DamageManager)
    }

}