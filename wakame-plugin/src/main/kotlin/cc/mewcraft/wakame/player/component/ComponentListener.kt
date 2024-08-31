package cc.mewcraft.wakame.player.component

import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.entity.Trident
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent

class ComponentListener : Listener {
    /**
     * 实现 [cc.mewcraft.wakame.item.components.ItemArrow] 组件的下列功能:
     * 穿透
     * 拾取
     * 视觉着火
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
        projectile.isVisualFire = itemArrow.hasVisualFire


        if (projectile is SpectralArrow) {
            projectile.glowingTicks = itemArrow.glowTicks
        }
    }

    /**
     * 实现 [cc.mewcraft.wakame.item.components.ItemArrow] 组件的下列功能:
     * 着火时间
     * 冰冻时间
     */
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity ?: return
        val projectile = event.entity
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        val nekoStack = projectile.itemStack.tryNekoStack ?: return
        val itemArrow = nekoStack.templates.get(ItemTemplateTypes.ARROW) ?: return
        if (hitEntity.fireTicks < itemArrow.fireTicks) hitEntity.fireTicks = itemArrow.fireTicks
        if (hitEntity.freezeTicks < itemArrow.frozenTicks) hitEntity.freezeTicks = itemArrow.frozenTicks
    }
}