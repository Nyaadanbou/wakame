package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.gui.catalog.item.ItemCatalogMainMenu
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

object CatalogCommands : CommandFactory<CommandSender> {
    private const val CATALOG_LITERAL = "catalog"
    private const val ITEM_LITERAL = "item"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands for catalog")
            ) {
                permission(CommandPermissions.CATALOG_ITEM)
                literal(CATALOG_LITERAL)
                literal(ITEM_LITERAL)
                optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { ctx ->
                    val sender = ctx.sender()
                    val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
                    val viewer = player?.single() ?: (sender as? Player) ?: run {
                        sender.sendPlainMessage("Player not found!")
                        return@handler
                    }

                    ThreadType.SYNC.launch {
                        sender.sendPlainMessage("Opening item catalog for player ${viewer.name}")
                        ItemCatalogMainMenu(viewer).open()
                    }
                }
            }.buildAndAdd(this)
        }
    }
}