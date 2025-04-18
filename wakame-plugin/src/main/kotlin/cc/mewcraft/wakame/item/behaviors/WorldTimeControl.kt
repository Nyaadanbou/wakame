package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.text.arguments
import cc.mewcraft.wakame.util.time.DurationFormatter
import cc.mewcraft.wakame.world.TimeControl
import io.papermc.paper.util.Tick
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

interface WorldTimeControl : ItemBehavior {
    private object Default : WorldTimeControl {
        override fun handleConsume(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemConsumeEvent) {
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

    companion object Type : ItemBehaviorType<WorldTimeControl> {
        override fun create(): WorldTimeControl = Default
    }
}