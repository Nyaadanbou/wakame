package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.damage.DamageIndicator
import cc.mewcraft.wakame.damage.TextIndicatorData
import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.joml.Vector3f
import kotlin.random.Random

class PacketDispatcher : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: WakameEntityDamageEvent) {
        val damager = when (val damager = event.damager) {
            is Player -> damager
            is Projectile -> damager.shooter as? Player ?: return
            else -> return
        }

        val damagee = event.damagee
        val damagePackets = event.damageMetadata.damageBundle.packets()

        val isCritical = event.damageMetadata.isCritical

        for (packet in damagePackets) {
            val packetDamage = packet.packetDamage.takeIf { it != 0.0 }
                ?.let { String.format("%.1f", it) }
                ?: continue

            // Get damagee center location
            val displayLocation = damagee.location.add(Random.nextDouble(-1.0, 1.0), damagee.height / 2 + Random.nextDouble(-0.5, 0.5), Random.nextDouble(-1.0, 1.0))
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
        val indicatorData = TextIndicatorData(
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
        val damageIndicator = DamageIndicator(indicatorData)
        damageIndicator.show(player)

        runTaskLater(2) {
            indicatorData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 5
                this.translation.add(0.0f, 0.5f, 0f)
                this.scale.set(2f, 2f, 2f)
            }

            damageIndicator.setEntityData(indicatorData)
            damageIndicator.refresh(player)
        }

        runTaskLater(8) {
            indicatorData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 15
                this.translation.add(0.0f, 1f, 0f)
                this.scale.set(0.0f, 0.0f, 0.0f)
            }

            damageIndicator.setEntityData(indicatorData)
            damageIndicator.refresh(player)
        }

        runTaskLater(26) {
            damageIndicator.hide(player)
        }
    }
}