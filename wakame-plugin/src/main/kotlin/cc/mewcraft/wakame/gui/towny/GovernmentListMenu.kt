package cc.mewcraft.wakame.gui.towny

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.integration.towny.Government
import cc.mewcraft.wakame.item.resolveToItemWrapper
import cc.mewcraft.wakame.util.cooldown.Cooldown
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataCooldownKey
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import java.util.concurrent.TimeUnit

sealed class GovernmentListMenu {

    abstract fun open()
}

abstract class PagedGovernmentListMenu(
    protected val viewer: Player,
) : GovernmentListMenu() {

    companion object {
        private val KEY_TELEPORT_COOLDOWN = metadataCooldownKey("government_list_menu:teleport_cooldown")
    }

    protected abstract val uiSettings: BasicMenuSettings

    protected abstract fun getGovernments(): List<Government>

    private val pagedGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*uiSettings.structure)
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('?', HintItem())
        builder.setContent(buildContents())
    }

    private val window: Window = Window.single()
        .setGui(pagedGui)
        .setTitle(uiSettings.title)
        .build(viewer)

    private fun buildContents(): List<Item> {
        val governments = getGovernments()
        return governments
            .filter { gov -> gov.canShow }
            .mapIndexed { index, gov ->
                GovernmentEntryItem(gov, index)
            }
    }

    override fun open() {
        window.open()
    }

    inner class BackgroundItem : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return uiSettings.getSlotDisplay("background").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // no-op
        }
    }

    inner class PrevItem : PageItem(false) {

        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage()) {
                return uiSettings.getSlotDisplay("background").resolveToItemWrapper()
            }
            return uiSettings.getSlotDisplay("prev_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(pagedGui.currentPage + 1))
                    component("total_page", Component.text(pagedGui.pageAmount))
                }
            }
        }
    }

    inner class NextItem : PageItem(true) {

        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasNextPage()) {
                return uiSettings.getSlotDisplay("background").resolveToItemWrapper()
            }
            return uiSettings.getSlotDisplay("next_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(pagedGui.currentPage + 1))
                    component("total_page", Component.text(pagedGui.pageAmount))
                }
            }
        }
    }

    /**
     * `提示占位`的图标.
     */
    inner class HintItem : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return uiSettings.getSlotDisplay("hint").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // no-op
        }
    }

    inner class GovernmentEntryItem(
        private val government: Government,
        private val index: Int,
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return uiSettings.getSlotDisplay("entry").resolveToItemWrapper {
                standard {
                    component("name", government.name)
                    component("index", Component.text(index + 1))
                }
                folded("board", government.board.map(Component::text))
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val cooldown = player.metadata().getOrPut(KEY_TELEPORT_COOLDOWN) { Cooldown.of(5, TimeUnit.SECONDS) }
            if (cooldown.test()) {
                government.teleport(player)
            }
        }
    }
}