package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.BlacksmithStationParser
import cc.mewcraft.wakame.command.parser.MergingTableParser
import cc.mewcraft.wakame.command.parser.ModdingTableParser
import cc.mewcraft.wakame.command.parser.RerollingTableParser
import cc.mewcraft.wakame.gui.blacksmith.BlacksmithMenu
import cc.mewcraft.wakame.gui.merge.MergingMenu
import cc.mewcraft.wakame.gui.mod.ModdingMenu
import cc.mewcraft.wakame.gui.reroll.RerollingMenu
import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStation
import cc.mewcraft.wakame.reforge.merge.MergingTable
import cc.mewcraft.wakame.reforge.mod.ModdingTable
import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import kotlin.jvm.optionals.getOrNull

internal object ReforgeCommand : KoishCommandFactory<CommandSender> {

    override fun KoishCommandFactory.Builder<CommandSender>.createCommands() {
        // Open a blacksmith menu for a player
        buildAndAdd {
            permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_BLACKSMITH))
            literal("reforge")
            literal("blacksmith")
            required("station", BlacksmithStationParser.blacksmithStationParser())
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenBlacksmithMenu)
        }

        // Open a merging menu for a player
        buildAndAdd {
            permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_MERGING))
            literal("reforge")
            literal("merge")
            required("table", MergingTableParser.mergingTableParser())
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenMergingTableMenu)
        }

        // Open a modding menu for a player
        buildAndAdd {
            permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_MODDING))
            literal("reforge")
            literal("mod")
            required("table", ModdingTableParser.moddingTableParser())
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenModdingTableMenu)
        }

        // Open a rerolling menu for a player
        buildAndAdd {
            permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_REROLLING))
            literal("reforge")
            literal("reroll")
            required("table", RerollingTableParser.rerollingTableParser())
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenRerollingTableMenu)
        }
    }

    private suspend fun handleOpenRerollingTableMenu(ctx: CommandContext<CommandSender>) {
        val sender = ctx.sender()
        val table = ctx.get<RerollingTable>("table")
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        val menu = RerollingMenu(table, viewer)
        withContext(Dispatchers.minecraft) { menu.open() }
    }

    private suspend fun handleOpenModdingTableMenu(ctx: CommandContext<CommandSender>) {
        val sender = ctx.sender()
        val table = ctx.get<ModdingTable>("table")
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        val menu = ModdingMenu(table, viewer)
        withContext(Dispatchers.minecraft) { menu.open() }
    }

    private suspend fun handleOpenMergingTableMenu(ctx: CommandContext<CommandSender>) {
        val sender = ctx.sender()
        val table = ctx.get<MergingTable>("table")
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        val menu = MergingMenu(table, viewer)
        withContext(Dispatchers.minecraft) { menu.open() }
    }

    private suspend fun handleOpenBlacksmithMenu(ctx: CommandContext<CommandSender>) {
        val sender = ctx.sender()
        val table = ctx.get<BlacksmithStation>("station")
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        val menu = BlacksmithMenu(table, viewer)
        withContext(Dispatchers.minecraft) { menu.open() }
    }

}