package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.entity.LivingEntity

/**
 * 负责初始化伤害系统的一些内部状态, 如 API 实例.
 */
@Init(stage = InitStage.PRE_WORLD)
internal object DamageApiBootstrap {

    @InitFun
    fun init() {
        // 注册 DamageApplier
        DamageApplier.register(BukkitDamageApplier)

        // 注册 DamageBundleFactory
        DamageBundleFactory.register(DefaultDamageBundleFactory)

        // 注册 DamageManagerApi
        DamageManagerApi.register(DamageManager)
    }

}

// ------------
// 内部实现
// ------------

internal object BukkitDamageApplier : DamageApplier {
    override fun damage(victim: LivingEntity, source: LivingEntity?, amount: Double) {
        // 这里仅仅用于触发一下 Bukkit 的 EntityDamageEvent.
        // 伤害数值填多少都无所谓, 最后都会被事件监听逻辑重新计算.
        victim.damage(amount, source)
    }
}

internal object DefaultDamageBundleFactory : DamageBundleFactory {
    override fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle {
        val packets = mutableMapOf<RegistryEntry<Element>, DamagePacket>()
        for ((id, packet) in data) {
            val element = BuiltInRegistries.ELEMENT.getEntry(id)
            if (element != null) {
                packets[element] = packet
            }
        }
        return DamageBundle(packets)
    }

    override fun create(data: Map<RegistryEntry<Element>, DamagePacket>): DamageBundle {
        return DamageBundle(data)
    }
}
