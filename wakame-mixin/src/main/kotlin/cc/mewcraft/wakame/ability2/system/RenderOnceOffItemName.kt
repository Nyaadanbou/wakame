package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.OnceOffItemName
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.canKoishify
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import cc.mewcraft.wakame.item2.network.ItemNameRenderer
import com.github.quillraven.fleks.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemHeldEvent

object RenderOnceOffItemName : ListenableIteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, InventoryListenable, OnceOffItemName) }
) {
    override fun onTickEntity(entity: Entity) {
        val onceOffItemName = entity[OnceOffItemName]
        if (onceOffItemName.component != onceOffItemName.lastComponent) {
            onceOffItemName.lastComponent = onceOffItemName.component
            val player = entity[BukkitPlayer].unwrap()
            ItemNameRenderer.send(onceOffItemName.index, onceOffItemName.component, player)
        }
        onceOffItemName.durationTick--
        if (onceOffItemName.durationTick <= 0) {
            entity.configure { it -= OnceOffItemName }
            ItemNameRenderer.resync(entity[BukkitPlayer].unwrap())
        }
    }

    @EventHandler
    fun on(event: PlayerItemHeldEvent) {
        val player = event.player
        if (player.canKoishify() && player.koishify().unwrap().has(OnceOffItemName)) {
            ItemNameRenderer.resync(player)
        }
    }
}