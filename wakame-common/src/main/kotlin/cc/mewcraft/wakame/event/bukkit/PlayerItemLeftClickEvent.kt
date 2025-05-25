package cc.mewcraft.wakame.event.bukkit

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * 当玩家使用物品点击左键时触发.
 *
 * 注意事项:
 * - 如果触发此事件的伤害/交互事件被取消, 此事件仍触发.
 * - 玩家空手点击不会触发此事件.
 * - 攻击矿车/船也会触发此事件.
 *
 * @param player 触发事件的玩家
 * @param item 使用的物品, 永远不为空气
 * @param hand 使用的手, 必为主手或副手
 */
class PlayerItemLeftClickEvent(
    player: Player,
    val item: ItemStack,
    val hand: EquipmentSlot, // As of 1.21.5, Minecraft can only fire this event for main hand
) : PlayerEvent(player) {

    init {
        require(!item.isEmpty) { "item cannot be empty" } // throw early

//        player.sendMessage(
//            "${Bukkit.getCurrentTick()} ${PlayerItemLeftClickEvent::class.simpleName} called, on = ${
//                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { stream ->
//                    stream.asSequence().dropWhile { frame ->
//                        frame.className == PlayerItemLeftClickEvent::class.qualifiedName
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