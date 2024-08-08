package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.MergingTableParser
import cc.mewcraft.wakame.command.parser.ModdingTableParser
import cc.mewcraft.wakame.command.parser.RerollingTableParser
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.gui.merge.MergingMenu
import cc.mewcraft.wakame.gui.mod.ModdingMenu
import cc.mewcraft.wakame.gui.reroll.RerollingMenu
import cc.mewcraft.wakame.reforge.merge.MergingTable
import cc.mewcraft.wakame.reforge.mod.ModdingTable
import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.util.coroutine.BukkitMain
import kotlinx.coroutines.Dispatchers
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

object ReforgeCommands : CommandFactory<CommandSender> {
    private const val REFORGE_LITERAL = "reforge"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands for reforge")
            ) {
                permission(CommandPermissions.REFORGE)
                literal(REFORGE_LITERAL)
                handler {
                    it.sender().sendPlainMessage("Reforge!")
                }
            }.buildAndAdd(this)

            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Open a merging menu for a player")
            ) {
                permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_MERGING))
                literal(REFORGE_LITERAL)
                literal("merge")
                required("table", MergingTableParser.mergingTableParser())
                optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                suspendingHandler(context = Dispatchers.BukkitMain) { ctx ->
                    val sender = ctx.sender()
                    val table = ctx.get<MergingTable>("table")
                    val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
                    val viewer = player?.single() ?: (sender as? Player) ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@suspendingHandler
                    }

                    val mergingMenu = MergingMenu(table, viewer)
                    sender.sendPlainMessage("Opening merging menu ...")
                    mergingMenu.open()
                }
            }.buildAndAdd(this)

            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Open a modding menu for a player")
            ) {
                permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_MODDING))
                literal(REFORGE_LITERAL)
                literal("mod")
                required("table", ModdingTableParser.moddingTableParser())
                optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                suspendingHandler(context = Dispatchers.BukkitMain) { ctx ->
                    val sender = ctx.sender()
                    val table = ctx.get<ModdingTable>("table")
                    val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
                    val viewer = player?.single() ?: (sender as? Player) ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@suspendingHandler
                    }

                    val moddingMenu = ModdingMenu(table, viewer)
                    sender.sendPlainMessage("Opening modding menu ...")
                    moddingMenu.open()
                }
            }.buildAndAdd(this)

            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Open a rerolling menu for a player")
            ) {
                permission(CommandPermissions.REFORGE.and(CommandPermissions.REFORGE_REROLLING))
                literal(REFORGE_LITERAL)
                literal("reroll")
                required("table", RerollingTableParser.rerollingTableParser())
                optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                suspendingHandler(context = Dispatchers.BukkitMain) { ctx ->
                    val sender = ctx.sender()
                    val table = ctx.get<RerollingTable>("table")
                    val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
                    val viewer = player?.single() ?: (sender as? Player) ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@suspendingHandler
                    }

                    val rerollingMenu = RerollingMenu(table, viewer)
                    sender.sendPlainMessage("Opening rerolling menu ...")
                    rerollingMenu.open()
                }
            }.buildAndAdd(this)
        }
    }
}