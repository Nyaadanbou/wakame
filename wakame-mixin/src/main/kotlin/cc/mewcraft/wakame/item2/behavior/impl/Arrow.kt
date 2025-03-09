package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.item2.NekoStack
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item2.template.ItemTemplateTypes
import org.bukkit.entity.*
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack

/**
 * 自定义箭矢的相关行为.
 */
interface Arrow : ItemBehavior {
    private object Default : Arrow {

        /**
         * 实现 [cc.mewcraft.wakame.item2.templates.components.ItemArrow] 组件的下列功能:
         * 穿透
         * 拾取
         * 箭矢着火时间
         * 发光时间
         */
        override fun handleItemProjectileLaunch(player: Player, itemStack: ItemStack, koishStack: NekoStack, projectile: Projectile, event: ProjectileLaunchEvent) {
            if (projectile !is AbstractArrow) return
            if (projectile is Trident) return

            val itemArrow = koishStack.templates.get(ItemTemplateTypes.ARROW) ?: return
            projectile.pierceLevel = itemArrow.pierceLevel
            projectile.pickupStatus = itemArrow.pickupStatus
            projectile.fireTicks = itemArrow.fireTicks


            if (projectile is SpectralArrow) {
                projectile.glowingTicks = itemArrow.glowTicks
            }
        }

        /**
         * 实现 [cc.mewcraft.wakame.item2.templates.components.ItemArrow] 组件的下列功能:
         * 命中着火时间
         * 命中冰冻时间
         */
        override fun handleItemProjectileHit(player: Player, itemStack: ItemStack, koishStack: NekoStack, projectile: Projectile, event: ProjectileHitEvent) {
            val hitEntity = event.hitEntity ?: return
            if (projectile !is AbstractArrow) return
            if (projectile is Trident) return

            val itemArrow = koishStack.templates.get(ItemTemplateTypes.ARROW) ?: return
            if (hitEntity.fireTicks < itemArrow.hitFireTicks) hitEntity.fireTicks = itemArrow.hitFireTicks
            if (hitEntity.freezeTicks < itemArrow.hitFrozenTicks) hitEntity.freezeTicks = itemArrow.hitFrozenTicks
        }
    }

    companion object Type : ItemBehaviorType<Arrow> {
        override fun create(): Arrow = Default
    }
}