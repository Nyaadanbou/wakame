package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.text.arguments
import cc.mewcraft.wakame.util.time.DurationFormatter
import cc.mewcraft.wakame.world.WeatherControl
import io.papermc.paper.util.Tick

object WorldWeatherControl : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack
        val weatherControl = itemstack.getProp(ItemPropTypes.WORLD_WEATHER_CONTROL) ?: return BehaviorResult.PASS

        if (!WeatherControl.isReady()) {
            player.sendMessage(
                TranslatableMessages.MSG_ERR_WORLD_WEATHER_CONTROL_NOT_READY.arguments(
                    DurationFormatter.format(Tick.of(WeatherControl.getTimeUntilReadyTicks()), true)
                )
            )
            return BehaviorResult.PASS
        }

        WeatherControl.execute(weatherControl.actions)
        player.sendMessage(TranslatableMessages.MSG_WORLD_WEATHER_CONTROL_EXECUTED)

        return BehaviorResult.PASS
    }
}