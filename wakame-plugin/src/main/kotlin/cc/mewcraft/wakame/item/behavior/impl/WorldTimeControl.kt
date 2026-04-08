package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.text.arguments
import cc.mewcraft.wakame.util.time.DurationFormatter
import cc.mewcraft.wakame.world.TimeControl
import io.papermc.paper.util.Tick

object WorldTimeControl : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack
        val timeControl = itemstack.getProp(ItemPropTypes.WORLD_TIME_CONTROL) ?: return BehaviorResult.PASS

        if (!TimeControl.isReady()) {
            player.sendMessage(
                TranslatableMessages.MSG_ERR_WORLD_TIME_CONTROL_NOT_READY.arguments(
                    DurationFormatter.format(Tick.of(TimeControl.getTimeUntilReadyTicks()), true)
                )
            )
            return BehaviorResult.PASS
        }

        when (timeControl.type) {
            TimeControl.ActionType.SET_TIME -> TimeControl.setTime(timeControl.time)
            TimeControl.ActionType.ADD_TIME -> TimeControl.addTime(timeControl.time)
        }
        player.sendMessage(TranslatableMessages.MSG_WORLD_TIME_CONTROL_EXECUTED)

        return BehaviorResult.PASS
    }
}