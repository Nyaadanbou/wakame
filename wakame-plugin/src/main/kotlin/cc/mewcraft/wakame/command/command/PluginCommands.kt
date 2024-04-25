package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.event.NekoReloadEvent
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.koin.core.component.KoinComponent

object PluginCommands : KoinComponent, CommandFactory<CommandSender> {
    private const val RELOAD_LITERAL = "reload"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> reload config
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Reloads the config files")
            ) {
                permission(CommandPermissions.PLUGIN)
                literal(RELOAD_LITERAL)
                literal("config")
                handler { context ->
                    val sender = context.sender()
                    sender.sendPlainMessage("Calling reload event ...")
                    NekoReloadEvent().callEvent()
                }
            }.buildAndAdd(this)
        }
    }
}