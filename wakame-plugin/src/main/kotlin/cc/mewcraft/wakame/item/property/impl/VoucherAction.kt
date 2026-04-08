package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 兑换券消耗后可执行的动作.
 *
 * 支持多态类型:
 * - [PlayerChat]: 以玩家身份发送聊天消息
 * - [PlayerCommand]: 以玩家身份执行指令
 * - [ConsoleCommand]: 以控制台身份执行指令
 */
interface VoucherAction {

    /**
     * 执行此动作.
     *
     * @param player 触发动作的玩家
     */
    fun execute(player: Player)

    companion object {

        @JvmField
        val SERIALIZER: SimpleSerializer<VoucherAction> = DispatchingSerializer.createPartial(
            mapOf(
                "player_chat" to PlayerChat::class,
                "player_command" to PlayerCommand::class,
                "console_command" to ConsoleCommand::class,
            )
        )
    }

    /**
     * 以玩家身份发送聊天消息.
     *
     * 支持 `{player}` 占位符, 将被替换为玩家名字.
     */
    @ConfigSerializable
    data class PlayerChat(
        @Setting("command")
        val command: String = "",
    ) : VoucherAction {
        override fun execute(player: Player) {
            player.chat(command.replace("{player}", player.name))
        }
    }

    /**
     * 以玩家身份执行指令.
     *
     * 支持 `{player}` 占位符, 将被替换为玩家名字.
     */
    @ConfigSerializable
    data class PlayerCommand(
        @Setting("command")
        val command: String = "",
    ) : VoucherAction {
        override fun execute(player: Player) {
            player.performCommand(command.replace("{player}", player.name))
        }
    }

    /**
     * 以控制台身份执行指令.
     *
     * 支持 `{player}` 占位符, 将被替换为玩家名字.
     */
    @ConfigSerializable
    data class ConsoleCommand(
        @Setting("command")
        val command: String = "",
    ) : VoucherAction {
        override fun execute(player: Player) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.name))
        }
    }
}
