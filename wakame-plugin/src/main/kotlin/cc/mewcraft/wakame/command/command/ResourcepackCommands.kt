package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.pack.ResourcePackPublisherProvider
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
                description = Description.of("Generates a server resourcepack")
            ) {
                permission(CommandPermissions.RESOURCEPACK)
                literal(RESOURCEPACK_LITERAL)
                literal("generate")
                suspendingHandler { context ->
                    val sender = context.sender()
                    val manager = get<ResourcePackManager>()

                    manager.generate(regenerate = true)
                        .onSuccess { sender.sendPlainMessage("Resourcepack has been generated successfully!") }
                        .onFailure { sender.sendPlainMessage("Failed to generate resourcepack: '${it.message}'") }
                }
            }.buildAndAdd(this)

            // /<root> resourcepack publish
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Publishes the server resourcepack")
            ) {
                permission(CommandPermissions.RESOURCEPACK)
                literal(RESOURCEPACK_LITERAL)
                literal("publish")
                suspendingHandler { context ->
                    val sender = context.sender()
                    val publisher = ResourcePackPublisherProvider.get()
                    publisher.publish()
                    sender.sendPlainMessage("Resourcepack has been published successfully!")
                }
            }.buildAndAdd(this)
        }
    }
}