package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.compatibility.mythicmobs.MythicMobsDamageApplier
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.component.get

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
        if (Injector.get<WakamePlugin>().isPluginPresent("MythicMobs")) {
            DamageApplierApi.register(MythicMobsDamageApplier)
        } else {
            DamageApplierApi.register(DamageApplier)
        }

        // 注册 DamageManagerApi
        DamageManagerApi.register(DamageManager)
    }
}