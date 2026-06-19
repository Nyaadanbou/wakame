package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentInitializer
import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

internal class CatalogEnchantmentByItemMenu(
    val viewer: Player,
) : CatalogEnchantmentMenu {

    private val settings: BasicMenuSettings = CatalogEnchantmentMenuSettings.getMenuSettings("by_item")

    private val inputSlot = VirtualInventory(1)

    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addCommonIngredients(settings)
        .addIngredient('i', inputSlot)
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(emptyList())
        .build()

    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setViewer(viewer)
        .setTitle(settings.title)
        .addCloseHandler { returnInputItem() }
        .build()

    init {
        inputSlot.addPreUpdateHandler { e ->
            when {
                e.isSwap -> e.isCancelled = true
                e.isAdd -> { /* allow, processed in post-update */ }
                e.isRemove -> {
                    e.isCancelled = true
                    returnInputItem()
                    primaryGui.setContent(emptyList())
                }
            }
        }
        inputSlot.addPostUpdateHandler { e ->
            if (e.isAdd) {
                val item = inputSlot.getItem(0)
                if (item != null) {
                    val entries = CatalogEnchantmentInitializer.entriesForItem(item)
                    primaryGui.setContent(entries.map { buildEnchantmentItem(it) })
                }
            }
        }
    }

    private fun returnInputItem() {
        val item = inputSlot.getItem(0)
        if (item != null) {
            inputSlot.setItem(UpdateReason.SUPPRESSED, 0, null)
            val overflow = viewer.inventory.addItem(item)
            overflow.forEach { (_, leftover) ->
                viewer.world.dropItem(viewer.location, leftover)
            }
        }
    }

    override fun open() {
        primaryWindow.open()
    }

    override fun close() {
        primaryWindow.close()
    }
}
