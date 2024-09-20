package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.hologram.Hologram
import cc.mewcraft.wakame.hologram.TextHologramData
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.joml.Vector3f
import kotlin.random.Random

class PacketDispatcher : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: NekoEntityDamageEvent) {
        val source = event.damageSource
        val damager = source.causingEntity as? Player ?: return

        val damagee = event.damagee

        val damageLocation = source.damageLocation ?: damagee.location.add(0.0, damagee.height / 2, 0.0)

        val damagePackets = event.damageMetadata.damageBundle.packets()

        val isCritical = event.damageMetadata.isCritical

        for (packet in damagePackets) {
            val packetDamage = packet.packetDamage.takeIf { it != 0.0 }
                ?.let { String.format("%.1f", it) }
                ?: continue

            // Get damagee center location
            val displayLocation = damageLocation.add(Random.nextDouble(-1.0, 1.0), Random.nextDouble(-0.5, 0.5), Random.nextDouble(-1.0, 1.0))
            val elementDamageText = text {
                content(packetDamage)
                style(packet.element.displayName.style())
            }

            val displayComponent = if (isCritical) {
                // Add critical hit emoji
                text {
                    content("\uD83D\uDCA5 ")
                    color(TextColor.color(0xff9900))
                    append(elementDamageText)
                }
            } else {
                elementDamageText
            }

            val scale = if (isCritical) Vector3f(1.5f, 1.5f, 1.5f) else Vector3f(1.0f, 1.0f, 1.0f)
            DamageDisplayHandler.summonDisplayTask(displayLocation, displayComponent, scale, damager)
        }
    }
}

private object DamageDisplayHandler {

    fun summonDisplayTask(
        location: Location,
        text: Component,
        scale: Vector3f,
        player: Player,
    ) {
        val hologramData = TextHologramData(
            location,
            text,
            Color.fromARGB(0),
            false,
            TextDisplay.TextAlignment.CENTER,
            true
        ).apply {
            this.scale = scale
            this.brightness = Display.Brightness(15, 0)
        }
        val hologram = Hologram(hologramData)
        hologram.show(player)

        runTaskLater(2) {
            hologramData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 5
                this.translation.add(0.0f, 0.5f, 0.0f)
                this.scale.set(2.0f, 2.0f, 2.0f)
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(player)
        }

        runTaskLater(8) {
            hologramData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 15
                this.translation.add(0.0f, 1.0f, 0.0f)
                this.scale.set(0.0f, 0.0f, 0.0f)
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(player)
        }

        runTaskLater(24) {
            hologram.hide(player)
        }
    }
}