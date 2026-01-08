package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.townflight.TownFlightIntegration
import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import org.bukkit.util.Vector

object TownyFlight : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack
        val townyFlight = itemstack.getProp(ItemPropTypes.TOWNY_FLIGHT) ?: return BehaviorResult.PASS

        try {
            TownFlightIntegration.addTempFlight(player, silent = false, seconds = townyFlight.duration)
        } catch (_: UnsupportedOperationException) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NOT_INSIDE_TOWN)
            return BehaviorResult.PASS
        } catch (_: Exception) {
            player.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR)
            return BehaviorResult.PASS
        }

        if (TownFlightIntegration.canFly(player, silent = true)) {
            if (townyFlight.rocketOnConsume) {
                player.isFlying = true
                player.velocity = Vector(.0, townyFlight.rocketForce, .0)
            }
        }

        return BehaviorResult.PASS
    }
}