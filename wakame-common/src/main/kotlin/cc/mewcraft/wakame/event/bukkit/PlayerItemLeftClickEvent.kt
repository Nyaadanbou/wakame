package cc.mewcraft.wakame.event.bukkit

import net.minecraft.server.MinecraftServer.currentTick
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

/**
 * 当玩家使用物品点击左键时触发.
 *
 * @param player 触发事件的玩家
 * @param item 使用的物品, 永远不为空气
 */
class PlayerItemLeftClickEvent(
    player: Player,
    val item: ItemStack,
) : PlayerEvent(player) {

    init {
        require(!item.isEmpty) { "Item cannot be empty" } // throw early

        // FIXME #363: 注释掉调试用的临时代码
        player.sendMessage(
            "$currentTick ${PlayerItemLeftClickEvent::class.simpleName} called, on = ${
                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { stream ->
                    stream.dropWhile { frame ->
                        frame.className == PlayerItemLeftClickEvent::class.qualifiedName
                    }.findFirst().getOrNull()?.methodType?.lastParameterType()?.simpleName
                }
            }"
        )
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