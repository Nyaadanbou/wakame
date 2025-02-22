package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.pack.ResourcePackPublisherProvider
import cc.mewcraft.wakame.pack.ResourcePackServiceProvider
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source
import kotlin.jvm.optionals.getOrNull

internal object ResourcepackCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.RESOURCEPACK)
            literal("resourcepack")
        }

        // <root> resourcepack generate
        // Generates a server resourcepack
        buildAndAdd(commonBuilder) {
            literal("generate")
            koishHandler(handler = ::handleGenerateResourcepack)
        }

        // <root> resourcepack publish
        // Publishes the server resourcepack
        buildAndAdd(commonBuilder) {
            literal("publish")
            koishHandler(handler = ::handlePublishResourcepack)
        }

        // <root> resourcepack resend
        // Resends the server resourcepack to specific players
        buildAndAdd(commonBuilder) {
            permission(CommandPermissions.RESOURCEPACK)
            literal("resend")
            optional("recipient", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            koishHandler(handler = ::handleResnedResourcepack)
        }
    }

    private suspend fun handleGenerateResourcepack(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val manager = Injector.get<ResourcePackManager>()

        try {
            manager.generate()
        } catch (e: Throwable) {
            sender.sendPlainMessage("Failed to generate resourcepack, see console for details.")
            LOGGER.error("Failed to generate resourcepack", e)
            return
        }
    }

    private fun handlePublishResourcepack(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val publisher = ResourcePackPublisherProvider.get()
        val result = publisher.publish()
        if (result) {
            sender.sendPlainMessage("Resourcepack has been published successfully!")
        } else {
            sender.sendPlainMessage("Resourcepack failed to publish, see console for more information")
        }
    }

    private fun handleResnedResourcepack(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val recipient = context.optional<MultiplePlayerSelector>("recipient")
            .getOrNull()
            ?.values()
            ?: (sender as? Player)?.let(::listOf)
            ?: emptyList()
        val service = ResourcePackServiceProvider.get()
        recipient.forEach(service::sendPack)
        sender.sendPlainMessage("Resourcepack has been sent successfully!")
    }

}