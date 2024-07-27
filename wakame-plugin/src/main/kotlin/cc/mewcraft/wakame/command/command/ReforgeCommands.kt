package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.ModdingTableParser
import cc.mewcraft.wakame.gui.modding.CoreModdingMenu
import cc.mewcraft.wakame.gui.modding.CurseModdingMenu
import cc.mewcraft.wakame.reforge.modding.ModdingTable
import cc.mewcraft.wakame.reforge.modding.ModdingType
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
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.parser.standard.EnumParser

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
                description = Description.of("Open a modding menu for a player")
            ) {
                permission(CommandPermissions.REFORGE)
                literal(REFORGE_LITERAL)
                literal("modding")
                required("type", EnumParser.enumParser(ModdingType::class.java))
                required("table", ModdingTableParser.moddingTableParser())
                optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { ctx ->
                    val sender = ctx.sender()
                    val type = ctx.get<ModdingType>("type")
                    val table = ctx.get<ModdingTable>("table")
                    val player = ctx.getOrNull<SinglePlayerSelector>("player")
                    val viewer = player?.single() ?: (sender as? Player) ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@handler
                    }

                    ThreadType.SYNC.launch {
                        when (type) {
                            ModdingType.CORE -> {
                                sender.sendPlainMessage("Opening menu for modding cores ...")
                                CoreModdingMenu(table, viewer).open()
                            }

                            ModdingType.CURSE -> {
                                sender.sendPlainMessage("Opening menu for modding curses ...")
                                CurseModdingMenu(table, viewer).open()
                            }
                        }
                    }
                }
            }.buildAndAdd(this)
        }
    }
}