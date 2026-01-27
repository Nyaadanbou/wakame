package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment.component.VoidEscape
import cc.mewcraft.wakame.integration.teleport.RandomTeleport
import cc.mewcraft.wakame.util.adventure.BukkitSound
import cc.mewcraft.wakame.util.metadata.Empty
import cc.mewcraft.wakame.util.metadata.Metadata
import cc.mewcraft.wakame.util.metadata.MetadataKey
import com.github.quillraven.fleks.Entity
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

object TickVoidEscapeEnchantment : ListenableIteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, VoidEscape) }
) {

    const val RANDOM_TELEPORT_RADIUS = 128.0
    const val RANDOM_TELEPORT_HEIGHT = 256.0

    @JvmStatic
    val RANDOM_TELEPORT_IN_PROGRESS = MetadataKey.createEmptyKey("elytra_extras:random_teleport_in_progress")

    override fun onTickEntity(entity: Entity) {
        // 无操作
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val playerEntity = player.koishify().unwrap()
        if (event.cause != EntityDamageEvent.DamageCause.VOID)
            return
        if (playerEntity.has(VoidEscape)) {
            // 从虚空掉落时触发随机传送效果:

            if (Metadata.provideForPlayer(player).has(RANDOM_TELEPORT_IN_PROGRESS))
                return

            val world = player.world
            val position = player.location.apply { y = 0.0 }

            // 标记随机传送正在进行中
            Metadata.provideForPlayer(player).put(RANDOM_TELEPORT_IN_PROGRESS, Empty.instance())

            RandomTeleport.execute(
                entity = player,
                world = world,
                center = position,
                radius = RANDOM_TELEPORT_RADIUS,
                height = RANDOM_TELEPORT_HEIGHT
            ).thenRun {
                // 清除随机传送进行中的标记
                Metadata.provideForPlayer(player).remove(RANDOM_TELEPORT_IN_PROGRESS)

                player.playSound(Sound.sound().type(BukkitSound.ITEM_TOTEM_USE).source(Sound.Source.PLAYER).build())
                player.playSound(Sound.sound().type(BukkitSound.ITEM_CHORUS_FRUIT_TELEPORT).source(Sound.Source.PLAYER).build())
            }.exceptionally {
                // 清除随机传送进行中的标记
                Metadata.provideForPlayer(player).remove(RANDOM_TELEPORT_IN_PROGRESS)

                LOGGER.warn("Failed to execute random teleport effect for player ${player.name}", it)
                null
            }
        }
    }
}