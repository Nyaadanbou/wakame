package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.damage.DamageIndicator
import cc.mewcraft.wakame.damage.TextIndicatorData
import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.TickableBuilder
import cc.mewcraft.wakame.tick.Ticker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.random.Random

class PacketDispatcher : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: WakameEntityDamageEvent) {
        val damager = event.damager
        if (damager !is Player)
            return

        val damagee = event.damagee
        val damagePackets = event.damageMetadata.damageBundle.packets()

        for (packet in damagePackets) {
            // Get damagee center location
            val displayLocation = damagee.location.clone().add(0.0, damagee.height / 2 + Random.nextDouble(0.0, 0.5), 0.0)
            val name = "${damagee.name}#${damagee.entityId}#${UUID.randomUUID()}"
            val displayText = Component.text(packet.packetDamage).style(Style.style(*packet.element.styles))
            DamageDisplayHandler.summonDisplayTask(displayLocation, damager, name, displayText)
        }
    }
}

private object DamageDisplayHandler : KoinComponent {
    private val ticker: Ticker by inject()

    fun summonDisplayTask(location: Location, damager: Player, name: String, text: Component) {
        val indicatorData = TextIndicatorData(
            name,
            location,
            text,
            Color.fromARGB(0, 0, 0, 0),
            true,
            TextDisplay.TextAlignment.CENTER,
            false
        )
        val damageIndicator = DamageIndicator(indicatorData)

        val displayTickable = TickableBuilder.newBuilder()
            .execute { tick ->
                if (tick == 0L) {
                    damageIndicator.show(damager)
                    return@execute TickResult.CONTINUE_TICK
                }
                if (tick >= 20) {
                    damageIndicator.hide(damager)
                    return@execute TickResult.ALL_DONE
                }
                damageIndicator.refresh(damager)
                TickResult.CONTINUE_TICK
            }

        ticker.schedule(displayTickable)
    }
}