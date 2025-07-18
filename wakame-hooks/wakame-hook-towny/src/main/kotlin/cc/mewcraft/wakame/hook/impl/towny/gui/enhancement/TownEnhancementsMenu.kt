package cc.mewcraft.wakame.hook.impl.towny.gui.enhancement

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.hook.impl.towny.bridge.koishify
import cc.mewcraft.wakame.hook.impl.towny.component.TownEnhancementType
import cc.mewcraft.wakame.hook.impl.towny.data.DataEnhancement
import cc.mewcraft.wakame.item2.display.resolveToItemWrapper
import cc.mewcraft.wakame.util.Identifiers
import com.palmergames.bukkit.towny.`object`.Town
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

internal class TownEnhancementsMenu(
    /**
     * 该菜单所依赖的城镇.
     */
    val town: Town,
    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) {

    /**
     * 该菜单的布局
     */
    private val settings: BasicMenuSettings
        get() = BasicMenuSettings(
            title = Component.text("TownEnhancements"),
            structure = arrayOf(
                ". . . . . . . . .",
                ". x . x . x . x .",
                ". x . x . x . x .",
                ". x . x . x . x .",
                "< . . . . . . . >"
            ),
            icons = hashMapOf(
                "background" to Identifiers.of("internal/menu/common/default/background"),
                "prev_page" to Identifiers.of("internal/menu/enhancement/prev_page"),
                "next_page" to Identifiers.of("internal/menu/enhancement/next_page"),
                "content_list_slot_horizontal" to Identifiers.of("internal/menu/enhancement/content_list_slot_horizontal")
            )
        )

    /**
     * 城镇增强菜单的 [Gui].
     *
     * - `.`: background
     * - `x`: enhancement
     * - `<`: prev_page
     * - `>`: next_page
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(settings.title)
        builder.setViewer(viewer)
    }

    fun open() {
        val enhancements = with(Fleks.INSTANCE.world) {
            DataEnhancement.fromTownHallEntity(town.koishify())
        }
        primaryGui.setContent(enhancements.map(::EnhancementItem))
        primaryWindow.open()
    }

    inner class EnhancementItem(
        private val enhancement: DataEnhancement,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return when (enhancement.type) {
                TownEnhancementType.BUFF_FURNACE -> ItemWrapper(ItemStack.of(Material.FURNACE).also { it.amount = enhancement.level })
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (clickType.isLeftClick) {
                val menu = TownEnhancementsPreviewMenu(
                    previous = this@TownEnhancementsMenu,
                    enhancement = enhancement
                )
                menu.open()
            }
        }
    }

    /**
     * 背景占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // do nothing
        }
    }

    /**
     * 上一页的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper()
        }
    }

    /**
     * 下一页的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return settings.getSlotDisplay("next_page").resolveToItemWrapper()
        }
    }
}