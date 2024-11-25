package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.initializer.Initializable

/**
 * 负责初始化伤害系统 API 的一些实例.
 */
internal object DamageBootstrap : Initializable {

    override fun onPreWorld() {
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