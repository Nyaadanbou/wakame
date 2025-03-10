package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.text.arguments
import cc.mewcraft.wakame.util.time.DurationFormatter
import cc.mewcraft.wakame.world.TimeControl
import io.papermc.paper.util.Tick
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

object WorldTimeControl : ItemBehavior {

    override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
        val timeControl = koishStack.templates.get(ItemTemplateTypes.WORLD_TIME_CONTROL) ?: return
        if (!TimeControl.isReady()) {
            event.isCancelled = true
            player.sendMessage(
                TranslatableMessages.MSG_ERR_WORLD_TIME_CONTROL_NOT_READY.arguments(
                    DurationFormatter.format(Tick.of(TimeControl.getTimeUntilReadyTicks()), true)
                )
            )
            return
        }

        when (timeControl.type) {
            TimeControl.ActionType.SET_TIME -> TimeControl.setTime(timeControl.time)
            TimeControl.ActionType.ADD_TIME -> TimeControl.addTime(timeControl.time)
        }
        player.sendMessage(TranslatableMessages.MSG_WORLD_TIME_CONTROL_EXECUTED)
    }

}