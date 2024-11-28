package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.StationParser
import cc.mewcraft.wakame.craftingstation.Station
import cc.mewcraft.wakame.gui.station.StationMenu
import cc.mewcraft.wakame.util.ThreadType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import kotlin.jvm.optionals.getOrNull

object StationCommands : CommandFactory<CommandSender> {
    private const val STATION_LITERAL = "station"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands for station")
            ) {
                permission(CommandPermissions.STATION)
                literal(STATION_LITERAL)
                handler { ctx ->
                    ctx.sender().sendPlainMessage("Crafting Station!")
                }
            }.buildAndAdd(this)

            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Open a station menu for a player")
            ) {
                permission(CommandPermissions.STATION)
                literal(STATION_LITERAL)
                required("station", StationParser.stationParser())
                optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { ctx ->
                    val sender = ctx.sender()
                    val station = ctx.get<Station>("station")
                    val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
                    val viewer = player?.single() ?: (sender as? Player) ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@handler
                    }

                    ThreadType.SYNC.launch {
                        sender.sendPlainMessage("Opening crafting station ${station.id} for player ${viewer.name}")
                        StationMenu(station, viewer).open()
                    }
                }
            }.buildAndAdd(this)
        }
    }
}