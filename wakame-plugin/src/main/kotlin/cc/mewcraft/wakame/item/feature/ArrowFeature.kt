package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.SpectralArrow
import org.bukkit.entity.Trident
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent

/**
 * 实现了自定义箭矢.
 */
@Init(stage = InitStage.POST_WORLD)
object ArrowFeature : Listener {

    init {
        registerEvents()
    }

    /**
     * 实现 [cc.mewcraft.wakame.item.property.impl.Arrow] 的下列功能:
     * - 穿透
     * - 拾取
     * - 箭矢着火时间
     * - 发光时间
     */
    @EventHandler(ignoreCancelled = true)
    fun on(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        // 弹射物实体不是箭矢也不是光灵箭 - 不处理
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        val itemstack = projectile.itemStack
        val arrow = itemstack.getProp(ItemPropTypes.ARROW) ?: return

        projectile.pierceLevel = arrow.pierceLevel
        projectile.pickupStatus = arrow.pickupStatus
        projectile.fireTicks = arrow.fireTicks
        if (projectile is SpectralArrow) {
            projectile.glowingTicks = arrow.glowTicks
        }
    }

    /**
     * 实现 [cc.mewcraft.wakame.item.property.impl.Arrow] 的下列功能:
     * - 命中着火时间
     * - 命中冰冻时间
     */
    @EventHandler(ignoreCancelled = true)
    fun on(event: ProjectileHitEvent) {
        val projectile = event.entity
        // 弹射物实体不是箭矢也不是光灵箭 - 不处理
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return
        // 未命中实体 - 不处理
        val hitEntity = event.hitEntity ?: return

        val itemstack = projectile.itemStack
        val arrow = itemstack.getProp(ItemPropTypes.ARROW) ?: return

        if (hitEntity.fireTicks < arrow.hitFireTicks) hitEntity.fireTicks = arrow.hitFireTicks
        if (hitEntity.freezeTicks < arrow.hitFrozenTicks) hitEntity.freezeTicks = arrow.hitFrozenTicks
    }

}