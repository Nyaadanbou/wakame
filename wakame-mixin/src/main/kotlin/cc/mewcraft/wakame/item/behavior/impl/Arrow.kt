package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ProjectileHitContext
import cc.mewcraft.wakame.item.behavior.ProjectileLaunchContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropertyTypes
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.SpectralArrow

/**
 * 自定义箭矢的相关行为.
 */
object Arrow : ItemBehavior {

    /**
     * 实现 [cc.mewcraft.wakame.item.property.impl.Arrow] 的下列功能:
     * - 穿透
     * - 拾取
     * - 箭矢着火时间
     * - 发光时间
     */
    override fun handleProjectileLaunch(context: ProjectileLaunchContext): BehaviorResult {
        val itemstack = context.itemstack
        val projectile = context.projectile
        if (projectile !is AbstractArrow) return BehaviorResult.PASS

        val arrowProperty = itemstack.getProp(ItemPropertyTypes.ARROW) ?: return BehaviorResult.PASS

        projectile.pierceLevel = arrowProperty.pierceLevel
        projectile.pickupStatus = arrowProperty.pickupStatus
        projectile.fireTicks = arrowProperty.fireTicks
        if (projectile is SpectralArrow) {
            projectile.glowingTicks = arrowProperty.glowTicks
        }

        return BehaviorResult.FINISH
    }

    /**
     * 实现 [cc.mewcraft.wakame.item.property.impl.Arrow] 的下列功能:
     * - 命中着火时间
     * - 命中冰冻时间
     */
    override fun handleProjectileHit(context: ProjectileHitContext): BehaviorResult {
        val itemstack = context.itemstack
        val projectile = context.projectile
        val hitEntity = context.hitEntity ?: return BehaviorResult.PASS
        if (projectile !is AbstractArrow) return BehaviorResult.PASS

        val arrowProperty = itemstack.getProp(ItemPropertyTypes.ARROW) ?: return BehaviorResult.PASS

        if (hitEntity.fireTicks < arrowProperty.hitFireTicks) hitEntity.fireTicks = arrowProperty.hitFireTicks
        if (hitEntity.freezeTicks < arrowProperty.hitFrozenTicks) hitEntity.freezeTicks = arrowProperty.hitFrozenTicks

        return BehaviorResult.FINISH
    }

}