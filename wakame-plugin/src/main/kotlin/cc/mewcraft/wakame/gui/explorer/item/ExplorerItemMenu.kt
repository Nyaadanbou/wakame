package cc.mewcraft.wakame.gui.explorer.item

import cc.mewcraft.wakame.entity.player.koishLevel
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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
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
        private val CONTENT_ITEM_CACHE: ArrayList<ContentItem> by ReloadableProperty { ArrayList() }
    }

    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "< . . . . . . . >",
        )
        builder.addIngredient('<', PREV_PAGE)
        builder.addIngredient('>', NEXT_PAGE)
        builder.addIngredient('.', BACKGROUND)
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.setContent(getGuiContents())
    }

    private val primaryWindow: Window = Window.single().apply {
        setGui(primaryGui)
        setViewer(viewer)
        setTitle("Item Explorer")
    }.build()

    fun getGuiContents(): List<ContentItem> {
        if (CONTENT_ITEM_CACHE.isEmpty() && !BuiltInRegistries.ITEM.isEmpty) {
            CONTENT_ITEM_CACHE.addAll(BuiltInRegistries.ITEM.getIndexedEntries().map(RegistryEntry<KoishItem>::unwrap).map(::ContentItem))
        }
        return CONTENT_ITEM_CACHE.toList()
    }

    fun open() {
        primaryWindow.open()
    }

    /**
     * `上一页` 的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return PREV_PAGE
        }
    }

    /**
     * `下一页` 的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return NEXT_PAGE
        }
    }

    /**
     * 物品库中的一个 Koish 物品的图标.
     */
    inner class ContentItem(
        private val item: KoishItem,
    ) : AbstractItem() {

        private val wrapper: ItemWrapper = ItemWrapper(KoishStackGenerator.generate(item, ItemGenerationContext(item, 0f)))

        override fun getItemProvider(): ItemProvider {
            return this.wrapper
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (clickType == ClickType.LEFT) {
                // 现场生成物品
                val context = ItemGenerationContext(item, 1f, player.koishLevel)
                val stack = KoishStackGenerator.generate(item, context)
                // 并给予玩家 (参考 minecraft:give)
                player.world.dropItem(player.location, stack) { item ->
                    item.setCanMobPickup(false)
                    item.owner = player.uniqueId
                    item.pickupDelay = 0
                }
            }
        }
    }
}