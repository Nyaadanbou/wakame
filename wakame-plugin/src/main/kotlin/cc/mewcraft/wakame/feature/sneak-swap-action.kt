package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable


// 玩家潜行换手时, 执行一系列可配置的动作


/**
 * 潜行换手时可执行的动作.
 */
interface SneakSwapAction {

    /**
     * 执行此动作.
     *
     * @param player 触发动作的玩家
     */
    fun execute(player: Player)

    companion object {
        fun serializer(): SimpleSerializer<SneakSwapAction> {
            return DispatchingSerializer.createPartial(
                mapOf(
                    "player_chat" to PlayerChat::class,
                    "player_command" to PlayerCommand::class,
                    "console_command" to ConsoleCommand::class,
                )
            )
        }
    }

    /**
     * 以玩家身份发送聊天消息.
     */
    @ConfigSerializable
    data class PlayerChat(
        val command: String = "",
    ) : SneakSwapAction {
        override fun execute(player: Player) {
            player.chat(command)
        }
    }

    /**
     * 以玩家身份执行指令.
     */
    @ConfigSerializable
    data class PlayerCommand(
        val command: String = "",
    ) : SneakSwapAction {
        override fun execute(player: Player) {
            player.performCommand(command)
        }
    }

    /**
     * 以控制台身份执行指令.
     */
    @ConfigSerializable
    data class ConsoleCommand(
        val command: String = "",
    ) : SneakSwapAction {
        override fun execute(player: Player) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        }
    }
}


class SneakSwapActionListener : Listener {

    private val actions by FEATURE_CONFIG.entryOrElse(emptyList<SneakSwapAction>(), "sneak_swap_actions")

    @EventHandler
    fun on(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (!player.isSneaking) return

        // 取消原版换手行为, 改为执行配置的动作
        event.isCancelled = true

        for (action in actions) {
            action.execute(player)
        }
    }
}
