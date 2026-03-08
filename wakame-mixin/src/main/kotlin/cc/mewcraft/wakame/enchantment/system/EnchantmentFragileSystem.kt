package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.effect.EnchantmentFragileEffect
import cc.mewcraft.wakame.util.metadata.metadata
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent
import kotlin.math.ceil

/**
 * @see cc.mewcraft.wakame.enchantment.effect.EnchantmentFragileEffect
 */
object EnchantmentFragileSystem : Listener {

    @EventHandler
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player
        val metadata = player.metadata()
        val fragile = metadata.getOrNull(EnchantmentFragileEffect.DATA_KEY) ?: return
        event.damage *= ceil(fragile.multiplier).toInt()
    }
}