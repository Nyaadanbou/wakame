package cc.mewcraft.wakame.player.component

import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent

class ComponentListener : Listener {
    /**
     * 实现 [cc.mewcraft.wakame.item.templates.components.ItemArrow] 组件的下列功能:
     * 穿透
     * 拾取
     * 箭矢着火时间
     * 发光时间
     */
    @EventHandler
    fun on(event: EntityShootBowEvent) {
        if (event.entity !is Player) return
        val projectile = event.projectile
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        val nekoStack = event.consumable?.tryNekoStack ?: return
        val itemArrow = nekoStack.templates.get(ItemTemplateTypes.ARROW) ?: return
        projectile.pierceLevel = itemArrow.pierceLevel
        projectile.pickupStatus = itemArrow.pickupStatus
        projectile.fireTicks = itemArrow.fireTicks


        if (projectile is SpectralArrow) {
            projectile.glowingTicks = itemArrow.glowTicks
        }
    }

    /**
     * 实现 [cc.mewcraft.wakame.item.templates.components.ItemArrow] 组件的下列功能:
     * 命中着火时间
     * 命中冰冻时间
     */
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity ?: return
        val projectile = event.entity
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        val nekoStack = projectile.itemStack.tryNekoStack ?: return
        val itemArrow = nekoStack.templates.get(ItemTemplateTypes.ARROW) ?: return
        if (hitEntity.fireTicks < itemArrow.hitFireTicks) hitEntity.fireTicks = itemArrow.hitFireTicks
        if (hitEntity.freezeTicks < itemArrow.hitFrozenTicks) hitEntity.freezeTicks = itemArrow.hitFrozenTicks
    }
}