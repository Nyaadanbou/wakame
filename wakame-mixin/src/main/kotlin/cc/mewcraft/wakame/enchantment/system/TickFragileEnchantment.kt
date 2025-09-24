package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment.component.Fragile
import com.github.quillraven.fleks.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemDamageEvent
import kotlin.math.ceil

/**
 * @see cc.mewcraft.wakame.enchantment.effect.EnchantmentFragileEffect
 */
object TickFragileEnchantment : ListenableIteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, Fragile) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作
    }

    @EventHandler
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val fragile = playerEntity.getOrNull(Fragile) ?: return

        event.damage = event.damage * ceil(fragile.multiplier).toInt()
    }

}