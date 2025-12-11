package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.integration.townynetwork.TownyNetworkIntegration
import cc.mewcraft.wakame.util.coroutine.async
import kotlinx.coroutines.Dispatchers
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.StringParser

internal object TownyNetworkCommand : KoishCommandFactory<Source> {

    private const val COMMAND_NAME = "townynetwork"

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val builder = build(COMMAND_NAME) {
            permission(CommandPermissions.TOWNY_NETWORK)
        }

        // <root> town spawn <server>
        buildAndAdd(builder) {
            senderType<PlayerSource>()
            literal("town")
            literal("spawn")
            required("server", StringParser.stringParser())
            koishHandler(context = Dispatchers.async, handler = ::handleTownSpawn)
        }

        // <root> nation spawn <server>
        buildAndAdd(builder) {
            senderType<PlayerSource>()
            literal("nation")
            literal("spawn")
            required("server", StringParser.stringParser())
            koishHandler(handler = ::handleNationSpawn)
        }
    }

    private suspend fun handleTownSpawn(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val server = context.get<String>("server")
        TownyNetworkIntegration.reqTownSpawn(sender, server)
    }

    private suspend fun handleNationSpawn(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val server = context.get<String>("server")
        TownyNetworkIntegration.reqNationSpawn(sender, server)
    }
}