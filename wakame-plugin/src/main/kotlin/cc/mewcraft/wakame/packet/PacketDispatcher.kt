package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import cc.mewcraft.wakame.util.randomOffset
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import it.unimi.dsi.fastutil.objects.Object2IntFunction
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.UUID

class PacketDispatcher : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: WakameEntityDamageEvent) {
        val damager = event.damager
        if (damager !is Player)
            return

        val damagee = event.damagee
        val location = damagee.location.randomOffset(1.0, 1.0, 1.0)
        DamageDisplayHandler.summonDisplayTask(location, damager)
    }
}

private object DamageDisplayHandler {
    private val entityUuid2EntityId = Object2IntOpenHashMap<UUID>()

    fun summonDisplayTask(location: Location, damager: Player) {
        val packetEventsAPI = PacketEvents.getAPI()
        val user = packetEventsAPI.playerManager.getUser(damager)
        val entityUniqueId = UUID.randomUUID()
        val entityId = entityUuid2EntityId.computeIfAbsent(entityUniqueId, Object2IntFunction { 1 })
        val packet = WrapperPlayServerSpawnEntity(
            entityId,
            entityUniqueId,
            EntityTypes.TEXT_DISPLAY,
            SpigotConversionUtil.fromBukkitLocation(location),
            0f,
            0,
            null
        )

    }
}