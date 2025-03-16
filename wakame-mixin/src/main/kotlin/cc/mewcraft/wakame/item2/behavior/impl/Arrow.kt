package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProperty
import org.bukkit.entity.*
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack

/**
 * 自定义箭矢的相关行为.
 */
data object Arrow : ItemBehavior {

    /**
     * 实现 [cc.mewcraft.wakame.item2.config.property.impl.Arrow] 的下列功能:
     * - 穿透
     * - 拾取
     * - 箭矢着火时间
     * - 发光时间
     */
    override fun handleItemProjectileLaunch(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileLaunchEvent) {
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        val arrowProperty = itemstack.getProperty(ItemPropertyTypes.ARROW) ?: return
        projectile.pierceLevel = arrowProperty.pierceLevel
        projectile.pickupStatus = arrowProperty.pickupStatus
        projectile.fireTicks = arrowProperty.fireTicks

        if (projectile is SpectralArrow) {
            projectile.glowingTicks = arrowProperty.glowTicks
        }
    }

    /**
     * 实现 [cc.mewcraft.wakame.item2.config.property.impl.Arrow] 的下列功能:
     * - 命中着火时间
     * - 命中冰冻时间
     */
    override fun handleItemProjectileHit(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity ?: return
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        val arrowProperty = itemstack.getProperty(ItemPropertyTypes.ARROW) ?: return
        if (hitEntity.fireTicks < arrowProperty.hitFireTicks) hitEntity.fireTicks = arrowProperty.hitFireTicks
        if (hitEntity.freezeTicks < arrowProperty.hitFrozenTicks) hitEntity.freezeTicks = arrowProperty.hitFrozenTicks
    }

}