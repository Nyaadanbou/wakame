package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.ItemParser
import cc.mewcraft.wakame.gui.explorer.item.ExplorerItemMenu
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.item.KoishStackGenerator
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
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

internal object ItemCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ITEM)
            literal("item")
        }

        // <root> item give <item> [amount] [player]
        // Give the player(s) certain amount of specific item(s)
        buildAndAdd(commonBuilder) {
            literal("give")
            required("item", ItemParser.itemParser())
            optional("amount", IntegerParser.integerParser(1, 4 * 9 * 64))
            optional("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            koishHandler(handler = ::handleGiveItem)
        }

        // <root> item explore [player]
        // 给特定玩家打开物品库浏览器
        buildAndAdd(commonBuilder) {
            literal("explore")
            optional("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            koishHandler(handler = ::handleExploreItem)
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
                    add(KoishStackGenerator.generate(item, ItemGenerationContext(item, 0f, 0)))
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

    private suspend fun handleExploreItem(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val viewers = context.getOrNull<MultiplePlayerSelector>("player")?.values()
            ?: (sender as? Player)?.let(::listOf)
            ?: emptyList()
        val menus = viewers.map(::ExplorerItemMenu)

        withContext(Dispatchers.minecraft) {
            menus.forEach(ExplorerItemMenu::open)
        }
    }

}
