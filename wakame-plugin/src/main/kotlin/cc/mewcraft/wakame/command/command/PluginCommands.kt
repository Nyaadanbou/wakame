package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import kotlin.system.measureTimeMillis

object PluginCommands : CommandFactory<CommandSender> {
    private const val RELOAD_LITERAL = "reload"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> = buildList {
        // /<root> reload config
        commandManager.commandBuilder(
            name = CommandConstants.ROOT_COMMAND,
            description = Description.of("Reloads the config files")
        ) {
            permission(CommandPermissions.PLUGIN)
            literal(RELOAD_LITERAL)
            literal("configs")
            suspendingHandler(context = Dispatchers.minecraft) { context ->
                val sender = context.sender()
                sender.sendPlainMessage("Calling command reload event ...")
                val event = NekoCommandReloadEvent()
                val reloadTime = measureTimeMillis {
                    event.callEvent()
                }
                sender.sendPlainMessage("Plugin has been reloaded successfully! ${reloadTime}ms elapsed.")
            }
        }.buildAndAdd(this)
    }
}