package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.catalog.item.Category
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.ItemCatalogCategoryParser
import cc.mewcraft.wakame.gui.catalog.item.ItemCatalogMenuStack
import cc.mewcraft.wakame.gui.catalog.item.menu.CategoryMenu
import cc.mewcraft.wakame.gui.catalog.item.menu.MainMenu
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

        // /<root> catalog item [player]
        buildAndAdd {
            permission(CommandPermissions.CATALOG_ITEM)
            literal("catalog")
            literal("item")
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleOpenMainCatalog)
        }

        // /<root> catalog item [player] <category>
        buildAndAdd {
            permission(CommandPermissions.CATALOG_ITEM)
            literal("catalog")
            literal("item")
            optional("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            optional("category", ItemCatalogCategoryParser.categoryParser())
            koishHandler(handler = ::handleOpenPartCatalog)
        }

    }

    private suspend fun handleOpenMainCatalog(ctx: CommandContext<Source>) {
        val sender = ctx.sender().source()
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        val menu = MainMenu(viewer)
        withContext(Dispatchers.minecraft) { menu.open() }
    }

    private suspend fun handleOpenPartCatalog(ctx: CommandContext<Source>) {
        val sender = ctx.sender().source()
        val category = ctx.get<Category>("category")
        val player = ctx.optional<SinglePlayerSelector>("player").getOrNull()
        val viewer = player?.single() ?: (sender as? Player) ?: run {
            sender.sendPlainMessage("Player not found!")
            return
        }

        withContext(Dispatchers.minecraft) { ItemCatalogMenuStack.rewrite(viewer, MainMenu(viewer), CategoryMenu(category, viewer)) }
    }

}