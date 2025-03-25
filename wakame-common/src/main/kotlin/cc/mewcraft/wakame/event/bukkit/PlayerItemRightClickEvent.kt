package cc.mewcraft.wakame.event.bukkit

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * 当玩家使用物品点击右键时触发.
 * 主/副手会各自触发一次该事件.
 *
 * @param player 触发事件的玩家
 * @param item 使用的物品, 永远不为空气
 * @param hand 使用的物品的 [Hand]
 */
class PlayerItemRightClickEvent(
    player: Player,
    val item: ItemStack,
    hand: EquipmentSlot,
) : PlayerEvent(player) {

    init {
        require(!item.isEmpty) { "Item cannot be empty" } // throw early

        //player.sendMessage(
        //    "$currentTick ${PlayerItemRightClickEvent::class.simpleName} called, hand = $hand, on = ${
        //        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { stream ->
        //            stream.dropWhile { frame ->
        //                frame.className == PlayerItemRightClickEvent::class.qualifiedName
        //            }.findFirst().getOrNull()?.methodType?.lastParameterType()?.simpleName
        //        }
        //    }"
        //)
    }

    val hand = when (hand) {
        EquipmentSlot.HAND -> Hand.MAIN_HAND
        EquipmentSlot.OFF_HAND -> Hand.OFF_HAND
        else -> throw IllegalArgumentException("Invalid hand: $hand")
    }

    enum class Hand {
        MAIN_HAND,
        OFF_HAND;
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

}