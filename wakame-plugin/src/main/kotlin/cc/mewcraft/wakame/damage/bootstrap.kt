package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.tag.TagEntry
import net.kyori.adventure.key.Key
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

        // 修改原版和伤害有关的部分标签
        modifyTags()
    }

    /**
     * 修改部分 [RegistryKey.ITEM] 标签和 [RegistryKey.DAMAGE_TYPE] 标签.
     * 以实现伤害相同的特定功能.
     */
    private fun modifyTags() {
        BootstrapContexts.LIFECYCLE_MANAGER_OWNED_BY_BOOTSTRAP.registerEventHandler(
            LifecycleEvents.TAGS.postFlatten(RegistryKey.ITEM)
        ) { event ->
            val registrar = event.registrar()

            // 清空 minecraft:swords 标签的内容
            // 目的是移除原版的横扫机制
            registrar.setTag(ItemTypeTagKeys.SWORDS, emptySet())
        }

        BootstrapContexts.LIFECYCLE_MANAGER_OWNED_BY_BOOTSTRAP.registerEventHandler(
            LifecycleEvents.TAGS.postFlatten(RegistryKey.DAMAGE_TYPE)
        ) { event ->
            val registrar = event.registrar()

            // 清空 minecraft:damages_helmet 标签的内容
            // 目的是移除原版下落的方块会对头盔造成大量耐久度损耗的机制
            // 双重保险
            // 现有伤害系统在不清空该标签的情况下, 也不会计算头盔对下落的方块伤害的减免, 即不计算 HARD_HAT 伤害修饰器
            registrar.setTag(DamageTypeTagKeys.DAMAGES_HELMET, emptySet())
            // 清空 minecraft:witch_resistant_to 标签的内容
            // 目的是移除原版女巫对魔法伤害的减免
            // 双重保险
            // 现有伤害系统在不清空该标签的情况下, 也同样不考虑女巫的对魔法伤害的减免, 即不计算 MAGIC 伤害修饰器
            registrar.setTag(DamageTypeTagKeys.WITCH_RESISTANT_TO, emptySet())
        }

        BootstrapContexts.LIFECYCLE_MANAGER_OWNED_BY_BOOTSTRAP.registerEventHandler(
            LifecycleEvents.TAGS.preFlatten(RegistryKey.DAMAGE_TYPE)
        ) { event ->
            val registrar = event.registrar()

            // 将玩家伤害添加至忽略无懈可击时间的标签中
            // 目的是让玩家伤害忽略无懈可击时间
            // 或许是因为原版 minecraft:bypasses_cooldown 默认为空
            // 所以paper的 DamageTypeTagKeys 中没有该标签, 因此手动创建
            registrar.setTag(
                DamageTypeTagKeys.create(Key.key("minecraft:bypasses_cooldown")),
                setOf(TagEntry.tagEntry(DamageTypeTagKeys.IS_PLAYER_ATTACK))
            )
        }
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
    override fun create(data: Map<RegistryEntry<Element>, DamagePacket>): DamageBundle {
        return DamageBundle.damageBundleOf(data)
    }

    override fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle {
        val packets = mutableMapOf<RegistryEntry<Element>, DamagePacket>()
        for ((id, packet) in data) {
            val element = BuiltInRegistries.ELEMENT.getEntry(id)
            if (element != null) {
                packets[element] = packet
            }
        }
        return DamageBundle.damageBundleOf(packets)
    }
}
