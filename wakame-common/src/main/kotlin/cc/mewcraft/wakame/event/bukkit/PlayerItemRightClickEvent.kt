package cc.mewcraft.wakame.event.bukkit

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * 当玩家使用物品点击右键时触发.
 *
 * 注意事项:
 * - 如果触发此事件的事件被取消, 此事件仍触发.
 * - 玩家空手点击不会触发此事件.
 * - 主/副手会各自触发一次此事件.
 * - 右键可交互的方块不会触发此事件.
 * - 右键矿车/船/盔甲架不会触发此事件.
 * - 玩家长按原版可使用的物品/带 Consumable 组件的物品会持续多次触发该事件.
 * - 欲处理玩家长按右键松开的情景, 使用 [io.papermc.paper.event.player.PlayerStopUsingItemEvent].
 *
 * @param player 触发事件的玩家
 * @param item 使用的物品, 永远不为空气
 * @param hand 使用的手, 必为主手或副手
 */
@Deprecated("已失效，即将移除")
class PlayerItemRightClickEvent(
    player: Player,
    val item: ItemStack,
    val hand: EquipmentSlot,
) : PlayerEvent(player) {

    init {
        require(!item.isEmpty) { "item cannot be empty" } // throw early

//        player.sendMessage(
//            "${Bukkit.getCurrentTick()} ${PlayerItemRightClickEvent::class.simpleName} called, hand = $hand, on = ${
//                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { stream ->
//                    stream.asSequence().dropWhile { frame ->
//                        frame.className == PlayerItemRightClickEvent::class.qualifiedName
//                    }.firstOrNull()?.methodType?.lastParameterType()?.simpleName
//                }
//            }"
//        )
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