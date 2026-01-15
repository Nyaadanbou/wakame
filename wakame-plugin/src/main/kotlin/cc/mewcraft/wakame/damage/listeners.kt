package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import io.papermc.paper.event.entity.EntityKnockbackEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent


/**
 * 伤害系统的监听器, 也是游戏内伤害计算逻辑的代码入口.
 */
@Init(InitStage.POST_WORLD)
internal object DamageListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManagerImpl.registerTrident(event)
    }

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        DamageManagerImpl.registerExactArrow(event)
    }

    // 在弹射物击中方块时移除记录的 DamageMetadata.
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (event.hitBlock == null) return
        DamageManagerImpl.unregisterProjectile(event.entity)
    }

    // 用于取消自定义伤害的击退.
    @EventHandler
    fun on(event: EntityKnockbackEvent) {
        if (DamageManagerImpl.unregisterCancelKnockback(event.entity)) {
            event.isCancelled = true
        }
    }

}

/**
 * 监听 Koish 伤害事件, 使其遵循保护系统的规则.
 */
@Init(InitStage.POST_WORLD)
internal object DamageIntegration : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PostprocessDamageEvent) {
        val damager = event.damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee as? LivingEntity ?: return
        event.isCancelled = !ProtectionManager.canHurtEntity(damager, damagee, null)
    }

}