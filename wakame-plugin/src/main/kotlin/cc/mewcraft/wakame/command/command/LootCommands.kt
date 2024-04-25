package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder

object LootCommands : CommandFactory<CommandSender> {
    private const val LOOT_LITERAL = "loot"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        // TODO("待掉落系统框架落地后补充")
        return buildList {
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands for loot")
            ) {
                permission(CommandPermissions.LOOT)
                literal(LOOT_LITERAL)
                handler {
                    it.sender().sendPlainMessage("Loot!")
                }
            }.buildAndAdd(this)
        }
    }
}