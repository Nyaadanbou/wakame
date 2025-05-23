package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.townflight.TownFlightManager
import cc.mewcraft.wakame.integration.townflight.TownyNotAvailable
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

interface TownFlight : ItemBehavior {
    private object Default : TownFlight {
        override fun handleConsume(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemConsumeEvent) {
            val townFlight = koishStack.templates.get(ItemTemplateTypes.TOWN_FLIGHT) ?: return
            TownFlightManager
                .addTempFlight(player, silent = false, seconds = townFlight.duration)
                .onFailure { ex ->
                    event.isCancelled = true
                    if (ex is TownyNotAvailable) {
                        player.sendMessage(TranslatableMessages.MSG_ERR_NOT_INSIDE_TOWN)
                    } else {
                        player.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR)
                    }
                }
                .onSuccess {
                    if (TownFlightManager.canFly(player, silent = true).getOrDefault(false)) {
                        if (townFlight.rocketOnConsume) {
                            player.isFlying = true
                            player.velocity = Vector(0.0, townFlight.rocketForce, 0.0)
                        }
                    }
                }
        }
    }

    companion object Type : ItemBehaviorType<TownFlight> {
        override fun create(): TownFlight = Default
    }
}