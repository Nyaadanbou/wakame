package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.Item2Parser
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishStackGenerator
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser

internal object Item2Command : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ITEM)
            literal("item2")
        }

        // <root> item2 give <item> <amount> <player>
        // Give the player(s) certain amount of specific item(s)
        buildAndAdd(commonBuilder) {
            literal("give")
            required("item", Item2Parser.itemParser())
            optional("amount", IntegerParser.integerParser(1, 4 * 9 * 64))
            optional("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            koishHandler(handler = ::handleGiveItem)
        }
    }

    private suspend fun handleGiveItem(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val item = context.get<KoishItem>("item")
        val amount = context.getOrNull("amount") ?: 1
        val recipients = context.getOrNull<MultiplePlayerSelector>("player")?.values()
            ?: (sender as? Player)?.let(::listOf)
            ?: emptyList()

        // 创建这个 map 有点耗时, 异步执行
        val itemstackMap: Map<Player, Array<ItemStack>> = recipients.associateWith { player ->
            buildList(amount) {
                repeat(amount) {
                    add(KoishStackGenerator.generate(item, Context(item)))
                }
            }
        }.mapValues { (_, items) ->
            items.toTypedArray()
        }

        withContext(Dispatchers.minecraft) {
            for ((player, items) in itemstackMap) {
                player.inventory.addItem(*items)
                player.sendPlainMessage("You received $amount item(s): ${item.id}")
            }
        }
    }

}
