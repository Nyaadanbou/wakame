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

object HephaestusCommands : CommandFactory<CommandSender> {
    private const val HEPHAESTUS_LITERAL = "hephaestus"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        // TODO("待模型系统框架落地后补充")
        return buildList {
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands for hephaestus")
            ) {
                permission(CommandPermissions.HEPHAESTUS)
                literal(HEPHAESTUS_LITERAL)
                handler { context ->
                    context.sender().sendPlainMessage("Hephaestus!")
                }
            }.buildAndAdd(this)
        }
    }
}