package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.effect.EnchantmentAntigravShotEffect
import cc.mewcraft.wakame.util.metadata.metadata
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent

object TickAntigravShotEnchantment : Listener {

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val metadata = player.metadata()
        if (!metadata.has(EnchantmentAntigravShotEffect.DATA_KEY)) return
        val projectile = event.projectile
        projectile.setGravity(false) // Paper 可以单独设置箭矢的存活时间, 所以不用担心玩家对天射箭然后导致箭矢长期驻留内存
    }
}