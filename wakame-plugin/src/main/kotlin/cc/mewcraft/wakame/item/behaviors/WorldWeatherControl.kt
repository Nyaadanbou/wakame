package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.projectNeko
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.world.WeatherControl
import io.papermc.paper.util.Tick
import me.lucko.helper.text3.arguments
import me.lucko.helper.time.DurationFormatter
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

interface WorldWeatherControl : ItemBehavior {
    private object Default : WorldWeatherControl {
        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            val nekoStack = itemStack.projectNeko()
            val weatherControl = nekoStack.templates.get(ItemTemplateTypes.WORLD_WEATHER_CONTROL) ?: return
            if (!WeatherControl.isReady()) {
                event.isCancelled = true
                player.sendMessage(
                    MessageConstants.MSG_ERR_WORLD_WEATHER_CONTROL_NOT_READY.arguments(
                        DurationFormatter.format(Tick.of(WeatherControl.getTimeUntilReadyTicks()), true)
                    )
                )
                return
            }

            WeatherControl.execute(weatherControl.actions)
            player.sendMessage(MessageConstants.MSG_WORLD_WEATHER_CONTROL_EXECUTED)
        }
    }

    companion object Type : ItemBehaviorType<WorldWeatherControl> {
        override fun create(): WorldWeatherControl {
            return Default
        }
    }
}