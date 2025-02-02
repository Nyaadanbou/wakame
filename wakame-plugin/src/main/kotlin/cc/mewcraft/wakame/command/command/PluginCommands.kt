package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.lifecycle.reloader.Reloader
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
    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> = buildList {
        // /<root> reload config
        commandManager.commandBuilder(
            CommandConstants.ROOT_COMMAND, Description.of("Reloads the config files")
        ) {
            permission(CommandPermissions.PLUGIN)
            literal("reload")
            literal("configs")
            suspendingHandler(context = Dispatchers.minecraft) { context ->
                context.sender().sendMessage("Start reloading process, it may take a while ...")
                val reloadTime = measureTimeMillis {
                    Configs.reload()
                    Reloader.reload()
                }
                context.sender().sendMessage("Koish has been reloaded successfully! ${reloadTime}ms elapsed.")
            }
        }.buildAndAdd(this)
    }
}