package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.damage.DamageIndicator
import cc.mewcraft.wakame.damage.TextIndicatorData
import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import kotlin.random.Random

class PacketDispatcher : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: WakameEntityDamageEvent) {
        val damager = event.damager
        if (damager !is Player)
            return

        val damagee = event.damagee
        val damagePackets = event.damageMetadata.damageBundle.packets()

        val isCritical = event.damageMetadata.isCritical

        for (packet in damagePackets) {
            // Get damagee center location
            val displayLocation = damagee.location.add(Random.nextDouble(-0.5, 0.5), damagee.height / 2 + Random.nextDouble(0.0, 0.5), Random.nextDouble(-0.5, 0.5))
            val displayComponent = if (isCritical) {
                Component.text("💥 ").color(TextColor.color(0xff9900))
                    .append(Component.text(packet.packetDamage).style(Style.style(*packet.element.styles)))
            } else {
                Component.text(packet.packetDamage).style(Style.style(*packet.element.styles))
            }
            DamageDisplayHandler.summonDisplayTask(displayLocation, damager, displayComponent)
        }
    }
}

private object DamageDisplayHandler {

    fun summonDisplayTask(location: Location, damager: Player, text: Component) {
        val indicatorData = TextIndicatorData(
            location,
            text,
            Color.fromARGB(0),
            true,
            TextDisplay.TextAlignment.CENTER,
            true
        ).apply {
            brightness = Display.Brightness(9, 0)
        }
        val damageIndicator = DamageIndicator(indicatorData)
        damageIndicator.show(damager)

        runTaskLater(2) {
            indicatorData.apply {
                startInterpolation = 0
                interpolationDuration = 20
                translation.add(0.0f, 0.5f, 0f)
                scale.add(0.5f, 0.5f, 0.5f)
            }

            damageIndicator.setEntityData(indicatorData)
            damageIndicator.refresh(damager)
        }

        runTaskLater(25) {
            damageIndicator.hide(damager)
        }
    }
}