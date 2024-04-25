package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.pack.ResourcePackManager
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object ResourcepackCommands : KoinComponent, CommandFactory<CommandSender> {
    private const val RESOURCEPACK_LITERAL = "resourcepack"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> resourcepack generate
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Generates the resourcepack")
            ) {
                literal(RESOURCEPACK_LITERAL)
                literal("generate")
                suspendingHandler { context ->
                    val sender = context.sender()
                    val resourcePackManager = get<ResourcePackManager>()
                    resourcePackManager.generate(reGenerate = true)
                        .onSuccess { sender.sendPlainMessage("Resourcepack has been generated successfully") }
                        .onFailure { sender.sendPlainMessage("Failed to generate resourcepack: $it") }
                }
            }.buildAndAdd(this)

            // /<root> resourcepack upload
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Starts the resourcepack service")
            ) {
                permission(CommandPermissions.RESOURCEPACK)
                literal(RESOURCEPACK_LITERAL)
                literal("upload")
                suspendingHandler { context ->
                    val sender = context.sender()
                    val resourcePackManager = get<ResourcePackManager>()
                    resourcePackManager.startServer()
                    sender.sendPlainMessage("Resourcepack service has been started")
                }
            }.buildAndAdd(this)
        }
    }
}