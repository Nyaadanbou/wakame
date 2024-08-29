package cc.mewcraft.wakame.player.component

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent

class ComponentListener : Listener {
    /**
     * 实现 Arrow 组件的穿透数量功能.
     */
    @EventHandler
    fun on(event: EntityShootBowEvent) {
        if (event.entity !is Player) return
        val projectile = event.projectile
        if (projectile !is AbstractArrow) return
        val nekoStack = event.consumable?.tryNekoStack ?: return
        val arrow = nekoStack.components.get(ItemComponentTypes.ARROW) ?: return
        projectile.pierceLevel = arrow.pierceLevel.coerceAtLeast(0).toInt()
    }
}