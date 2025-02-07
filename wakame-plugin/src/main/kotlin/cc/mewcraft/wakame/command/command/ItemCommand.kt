package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.ItemParser
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.parser.standard.IntegerParser

internal object ItemCommand : KoishCommandFactory<CommandSender> {

    override fun KoishCommandFactory.Builder<CommandSender>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ITEM)
            literal("item")
        }

        // <root> item give <item> <amount> <player>
        // Give the player(s) certain amount of specific item(s)
        buildAndAdd(commonBuilder) {
            literal("give")
            required("item", ItemParser.itemParser())
            optional("amount", IntegerParser.integerParser(1, 4 * 9 * 64))
            optional("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            koishHandler(handler = ::handleGiveItem)
        }
    }

    private suspend fun handleGiveItem(context: CommandContext<CommandSender>) {
        val sender = context.sender()
        val item = context.get<NekoItem>("item")
        val amount = context.getOrNull("amount") ?: 1
        val recipients = context.getOrNull<MultiplePlayerSelector>("player")?.values()
            ?: (sender as? Player)?.let(::listOf)
            ?: emptyList()

        // 创建这个 map 有点耗时, 异步执行
        // Map<Player, List<ItemStack>>
        val itemStackMap = recipients.associateWith { player ->
            buildList(amount) {
                repeat(amount) {
                    add(item.realize(player.toUser()).wrapped)
                }
            }
        }.mapValues { (_, items) ->
            items.toTypedArray()
        }

        withContext(Dispatchers.minecraft) {
            for ((player, items) in itemStackMap) {
                player.inventory.addItem(*items)
                player.sendPlainMessage("You received $amount item(s): ${item.id}")
            }
        }
    }

}
