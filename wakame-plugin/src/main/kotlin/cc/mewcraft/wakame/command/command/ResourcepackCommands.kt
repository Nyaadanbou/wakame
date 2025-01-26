package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.pack.ResourcePackPublisherProvider
import cc.mewcraft.wakame.pack.ResourcePackServiceProvider
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import kotlin.jvm.optionals.getOrNull

object ResourcepackCommands : CommandFactory<CommandSender> {
    private const val RESOURCEPACK_LITERAL = "resourcepack"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> = buildList {
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
                val manager = Injector.get<ResourcePackManager>()

                try {
                    manager.generate()
                } catch (e: Throwable) {
                    sender.sendPlainMessage("Failed to generate resourcepack, see console for details.")
                    LOGGER.error("Failed to generate resourcepack", e)
                    return@suspendingHandler
                }
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
                val result = publisher.publish()
                if (result) {
                    sender.sendPlainMessage("Resourcepack has been published successfully!")
                } else {
                    sender.sendPlainMessage("Resourcepack failed to publish, see console for more information")
                }
            }
        }.buildAndAdd(this)

        // /<root> resourcepack resend
        commandManager.commandBuilder(
            name = CommandConstants.ROOT_COMMAND,
            description = Description.of("Resends the server resourcepack")
        ) {
            permission(CommandPermissions.RESOURCEPACK)
            literal(RESOURCEPACK_LITERAL)
            literal("resend")
            optional("recipient", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            suspendingHandler { context ->
                val sender = context.sender()
                val recipient = context.optional<MultiplePlayerSelector>("recipient")
                    .getOrNull()
                    ?.values()
                    ?: (sender as? Player)?.let(::listOf)
                    ?: emptyList()
                val service = ResourcePackServiceProvider.get()
                recipient.forEach(service::sendPack)
                sender.sendPlainMessage("Resourcepack has been sent successfully!")
            }
        }.buildAndAdd(this)
    }
}