package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.ItemCatalogCategoryParser
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemCategoryMenu
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMainMenu
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMenuStacks
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source
import kotlin.jvm.optionals.getOrNull

internal object CatalogCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {

        // /<root> catalog item [player] [category]
        buildAndAdd {
            permission(CommandPermissions.CATALOG_ITEM)
            literal("catalog")
            literal("item")
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            optional("category", ItemCatalogCategoryParser.categoryParser())
            koishHandler(handler = ::handleOpenItemCatalog)
        }

        // /<root> catalog kizami [player]
        buildAndAdd {
            permission(CommandPermissions.CATALOG_KIZAMI)
            literal("catalog")
            literal("kizami")
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenKizamiCatalog)
        }
    }

    private suspend fun handleOpenItemCatalog(ctx: CommandContext<Source>) {
        val sender = ctx.sender().source()
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val category = ctx.optional<CatalogItemCategory>("category").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run { sender.sendPlainMessage("Player not found!"); return }
        if (category == null) {
            // 如果未指定类别, 则优先打开最近一次看过的菜单
            val last = CatalogItemMenuStacks.peek(viewer)
            if (last != null) {
                withContext(Dispatchers.minecraft) { last.open() }
                return
            }
            val mainMenu = CatalogItemMainMenu(viewer)
            withContext(Dispatchers.minecraft) { CatalogItemMenuStacks.rewrite(viewer, mainMenu) }
        } else {
            val mainMenu = CatalogItemMainMenu(viewer)
            val categoryMenu = CatalogItemCategoryMenu(category, viewer)
            withContext(Dispatchers.minecraft) { CatalogItemMenuStacks.rewrite(viewer, mainMenu, categoryMenu) }
        }
    }

    private suspend fun handleOpenKizamiCatalog(ctx: CommandContext<Source>) {
        val sender = ctx.sender().source()
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run { sender.sendPlainMessage("Player not found!"); return }
    }
}