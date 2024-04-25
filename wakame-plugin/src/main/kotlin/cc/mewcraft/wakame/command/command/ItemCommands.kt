package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.ItemParser
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.reify
import cc.mewcraft.wakame.user.toUser
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
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
                required("amount", IntegerParser.integerParser(1, 99))
                required("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                suspendingHandler { context ->
                    val item = context.get<NekoItem>("item")
                    val amount = context.get<Int>("amount")
                    val multiplePlayerSelector = context.get<MultiplePlayerSelector>("player")
                    multiplePlayerSelector.values().forEach { player ->
                        val stack = item.reify(player.toUser())
                        repeat(amount) { player.inventory.addItem(stack.itemStack) }
                        player.sendPlainMessage("You received $amount item(s): ${item.key}")
                    }
                }
            }.buildAndAdd(this)
        }
    }
}
