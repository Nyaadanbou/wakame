package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage

/**
 * 负责初始化伤害系统 API 的一些实例.
 */
@Init(
    stage = InitStage.PRE_WORLD
)
internal object DamageBootstrap {

    @InitFun
    private fun init() {
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