package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.damage.DamageIndicator
import cc.mewcraft.wakame.damage.TextIndicatorData
import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.TickableBuilder
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.util.randomOffset
import me.lucko.helper.text3.mini
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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

private object DamageDisplayHandler : KoinComponent {
    private val ticker: Ticker by inject()

    fun summonDisplayTask(location: Location, damager: Player) {
        val indicatorData = TextIndicatorData(
            "name",
            location,
            "wda".mini,
            Color.BLUE,
            true,
            TextDisplay.TextAlignment.LEFT,
            false
        )
        val damageIndicator = DamageIndicator(indicatorData)

        val displayTickable = TickableBuilder.newBuilder()
            .execute { tick ->
                damageIndicator.show(damager)
                if (tick >= 20) {
                    damageIndicator.hide(damager)
                    return@execute TickResult.ALL_DONE
                }
                TickResult.CONTINUE_TICK
            }

        ticker.schedule(displayTickable)
    }
}