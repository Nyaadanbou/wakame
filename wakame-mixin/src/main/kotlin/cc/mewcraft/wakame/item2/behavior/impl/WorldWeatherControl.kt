package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.text.arguments
import cc.mewcraft.wakame.util.time.DurationFormatter
import cc.mewcraft.wakame.world.WeatherControl
import io.papermc.paper.util.Tick
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

object WorldWeatherControl : ItemBehavior {

    override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
        val weatherControl = koishStack.templates.get(ItemTemplateTypes.WORLD_WEATHER_CONTROL) ?: return
        if (!WeatherControl.isReady()) {
            event.isCancelled = true
            player.sendMessage(
                TranslatableMessages.MSG_ERR_WORLD_WEATHER_CONTROL_NOT_READY.arguments(
                    DurationFormatter.format(Tick.of(WeatherControl.getTimeUntilReadyTicks()), true)
                )
            )
            return
        }

        WeatherControl.execute(weatherControl.actions)
        player.sendMessage(TranslatableMessages.MSG_WORLD_WEATHER_CONTROL_EXECUTED)
    }

}