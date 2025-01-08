package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.util.coroutine.BukkitMain
import kotlinx.coroutines.Dispatchers
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.koin.core.component.KoinComponent
import kotlin.system.measureTimeMillis

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
                suspendingHandler(context = Dispatchers.BukkitMain) { context ->
                    val sender = context.sender()
                    sender.sendPlainMessage("Calling command reload event ...")
                    val event = NekoCommandReloadEvent()
                    val reloadTime = measureTimeMillis {
                        event.callEvent()
                        PluginEventBus.get().post(event)
                    }
                    sender.sendPlainMessage("Plugin has been reloaded successfully! ${reloadTime}ms elapsed.")
                }
            }.buildAndAdd(this)
        }
    }
}