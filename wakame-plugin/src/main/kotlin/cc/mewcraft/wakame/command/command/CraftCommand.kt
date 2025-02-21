package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.CraftingStationParser
import cc.mewcraft.wakame.craftingstation.CraftingStation
import cc.mewcraft.wakame.gui.craftingstation.CraftingStationMenu
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source
import kotlin.jvm.optionals.getOrNull

internal object CraftCommand : KoishCommandFactory<Source> {

    // TODO 各种 “station” 应该划为一类指令

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        buildAndAdd {
            permission(CommandPermissions.CRAFT)
            literal("craft")
            literal("station")
            required("station", CraftingStationParser.stationParser())
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenCraftingStation)
        }
    }

    private suspend fun handleOpenCraftingStation(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val station = context.get<CraftingStation>("station")
        val player = context.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        sender.sendPlainMessage("Opening crafting station ${station.id} for player ${viewer.name}")

        val menu = CraftingStationMenu(station, viewer)
        withContext(Dispatchers.minecraft) { menu.open() }
    }

}