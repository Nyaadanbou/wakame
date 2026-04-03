package cc.mewcraft.wakame.gui.explorer.item

import cc.mewcraft.wakame.entity.player.powerLevel
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.item.KoishStackGenerator
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.ReloadableProperty
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window

internal class ExplorerItemMenu(
    val viewer: Player,
) {

    companion object {
        // items
        private val PREV_PAGE = ItemWrapper(ItemStack.of(Material.ARROW).apply { setData(DataComponentTypes.ITEM_NAME, Component.text("Prev Page")) })
        private val NEXT_PAGE = ItemWrapper(ItemStack.of(Material.ARROW).apply { setData(DataComponentTypes.ITEM_NAME, Component.text("Next Page")) })
        private val BACKGROUND = ItemWrapper(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).apply { setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true)) })

        // caches
        private val CONTENT_ITEM_CACHE: ArrayList<Item> by ReloadableProperty { ArrayList() }
    }

    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "< . . . . . . . >",
        )
        .addIngredient(
            '<', BoundItem.pagedBuilder()
                .setItemProvider { _, _ ->
                    PREV_PAGE
                }
                .addClickHandler { _, gui, _ ->
                    gui.page -= 1
                }
        )
        .addIngredient(
            '>', BoundItem.pagedBuilder()
                .setItemProvider { _, _ ->
                    NEXT_PAGE
                }
                .addClickHandler { _, gui, _ ->
                    gui.page += 1
                }
        )
        .addIngredient('.', BACKGROUND)
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(getGuiContents())
        .build()

    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setViewer(viewer)
        .setTitle("Item Explorer")
        .build()

    fun getGuiContents(): List<Item> {
        if (CONTENT_ITEM_CACHE.isEmpty() && !BuiltInRegistries.ITEM.isEmpty) {
            CONTENT_ITEM_CACHE.addAll(BuiltInRegistries.ITEM.getIndexedEntries().map(RegistryEntry<KoishItem>::unwrap).map { koishItem ->
                Item.builder()
                    .setItemProvider { _ ->
                        ItemWrapper(KoishStackGenerator.generate(koishItem, ItemGenerationContext(koishItem, 0f)))
                    }
                    .addClickHandler { _, click ->
                        if (click.clickType == ClickType.LEFT) {
                            // 现场生成物品并给予玩家 (参考 minecraft:give)
                            val context = ItemGenerationContext(koishItem, 1f, click.player.powerLevel)
                            val stack = KoishStackGenerator.generate(koishItem, context)
                            click.player.world.dropItem(click.player.location, stack) { item ->
                                item.setCanMobPickup(false)
                                item.owner = click.player.uniqueId
                                item.pickupDelay = 0
                            }
                        }
                    }
                    .build()
            })
        }
        return CONTENT_ITEM_CACHE.toList()
    }

    fun open() {
        primaryWindow.open()
    }
}