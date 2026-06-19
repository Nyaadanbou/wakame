package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item

/**
 * 为魔咒图鉴的分页菜单添加公共装饰: 背景 `.`、上一页 `<`、下一页 `>`、返回 `b`.
 *
 * 各菜单仍各自定义内容槽位 (`x`/`i`) 与 [PagedGui.Builder.setContent].
 */
internal fun PagedGui.Builder<Item>.addCommonIngredients(settings: BasicMenuSettings): PagedGui.Builder<Item> {
    return this
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ -> settings.getIcon("background").resolveToItemWrapper() }
        )
        .addIngredient(
            '<', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page <= 0) settings.getIcon("background").resolveToItemWrapper()
                    else settings.getIcon("prev_page").resolveToItemWrapper {
                        standard {
                            component("current_page", Component.text(gui.page + 1))
                            component("total_page", Component.text(gui.pageCount))
                        }
                    }
                }
                .addClickHandler { _, gui, _ -> gui.page -= 1 }
        )
        .addIngredient(
            '>', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page >= gui.pageCount - 1) settings.getIcon("background").resolveToItemWrapper()
                    else settings.getIcon("next_page").resolveToItemWrapper {
                        standard {
                            component("current_page", Component.text(gui.page + 1))
                            component("total_page", Component.text(gui.pageCount))
                        }
                    }
                }
                .addClickHandler { _, gui, _ -> gui.page += 1 }
        )
        .addIngredient(
            'b', Item.builder()
                .setItemProvider { _ -> settings.getIcon("back").resolveToItemWrapper() }
                .addClickHandler { _, click -> CatalogEnchantmentMenuStacks.pop(click.player) }
        )
}
