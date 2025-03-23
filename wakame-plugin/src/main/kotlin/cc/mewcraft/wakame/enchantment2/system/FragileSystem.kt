package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.Fragile
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemDamageEvent

// 易碎: 增加单次消耗的耐久度。
object FragileSystem : ListenableIteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, Fragile) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作
    }

    @EventHandler
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val fragile = playerEntity.getOrNull(Fragile) ?: return

        event.damage = event.damage * fragile.multiplier

        LOGGER.info("PlayerItemDamageEvent passed to FragileSystem")
    }

}