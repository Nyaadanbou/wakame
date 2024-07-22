package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.gui.modding.CoreModdingMenu
import cc.mewcraft.wakame.gui.modding.CurseModdingMenu
import cc.mewcraft.wakame.util.ThreadType
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull

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
                description = Description.of("Open core modding menu for a player")
            ) {
                permission(CommandPermissions.REFORGE)
                literal(REFORGE_LITERAL)
                literal("modding_core")
                required("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { ctx ->
                    val sender = ctx.sender()
                    val player = ctx.getOrNull<SinglePlayerSelector>("player")
                    val viewer = player?.single() ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@handler
                    }

                    ThreadType.SYNC.launch {
                        sender.sendPlainMessage("Opening menu...")
                        val menu = CoreModdingMenu(viewer)
                        menu.open()
                    }
                }
            }.buildAndAdd(this)

            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Open curse modding menu for a player")
            ) {
                permission(CommandPermissions.REFORGE)
                literal(REFORGE_LITERAL)
                literal("modding_curse")
                required("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { ctx ->
                    val sender = ctx.sender()
                    val player = ctx.getOrNull<SinglePlayerSelector>("player")
                    val viewer = player?.single() ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@handler
                    }

                    ThreadType.SYNC.launch {
                        sender.sendPlainMessage("Opening menu...")
                        val menu = CurseModdingMenu(viewer)
                        menu.open()
                    }
                }
            }.buildAndAdd(this)
        }
    }
}