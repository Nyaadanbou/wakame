package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.text.arguments
import cc.mewcraft.wakame.util.time.DurationFormatter
import cc.mewcraft.wakame.world.WeatherControl
import io.papermc.paper.util.Tick
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

interface WorldWeatherControl : ItemBehavior {
    private object Default : WorldWeatherControl {
        override fun handleConsume(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemConsumeEvent) {
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

    companion object Type : ItemBehaviorType<WorldWeatherControl> {
        override fun create(): WorldWeatherControl {
            return Default
        }
    }
}