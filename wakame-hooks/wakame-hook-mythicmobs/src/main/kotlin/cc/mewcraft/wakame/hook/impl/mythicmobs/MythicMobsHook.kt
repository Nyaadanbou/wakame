package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.damage.DamageApplier
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.ConfigListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.DamageListener
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.registerEvents

@Hook(plugins = ["MythicMobs"])
object MythicMobsHook {
    init {
        // 注册 Listeners
        ConfigListener.registerEvents()
        DamageListener.registerEvents()

        // 注册 DamageApplier
        // 这应该覆盖掉默认的实例
        DamageApplier.register(MythicMobsDamageApplier)
    }
}