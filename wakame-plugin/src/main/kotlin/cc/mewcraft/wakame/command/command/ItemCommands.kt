package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.ItemParser
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.coroutine.BukkitMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.parser.standard.IntegerParser

object ItemCommands : CommandFactory<CommandSender> {
    private const val ITEM_LITERAL = "item"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> item give <item> <amount> <player>
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Give the player(s) specific item(s)")
            ) {
                permission(CommandPermissions.ITEM)
                literal(ITEM_LITERAL)
                literal("give")
                required("item", ItemParser.itemParser())
                optional("amount", IntegerParser.integerParser(1, 4 * 9 * 64))
                optional("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                suspendingHandler { context ->
                    val sender = context.sender()
                    val item = context.get<NekoItem>("item")
                    val amount = context.getOrNull<Int>("amount") ?: 1
                    val recipients = context.getOrNull<MultiplePlayerSelector>("player")?.values()
                        ?: (sender as? Player)?.let(::listOf)
                        ?: emptyList()

                    recipients.forEach { player ->
                        val itemStackFlow = sequence {
                            repeat(amount) {
                                yield(item.realize(player.toUser()).unsafe.handle)
                            }
                        }
                        val itemStacks = itemStackFlow.toList().toTypedArray()

                        withContext(Dispatchers.BukkitMain) {
                            player.inventory.addItem(*itemStacks)
                            player.sendPlainMessage("You received $amount item(s): ${item.key}")
                        }
                    }
                }
            }.buildAndAdd(this)
        }
    }
}
