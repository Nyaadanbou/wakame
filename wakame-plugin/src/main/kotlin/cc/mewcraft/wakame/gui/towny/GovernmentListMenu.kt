package cc.mewcraft.wakame.gui.towny

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.integration.townybridgelocal.MenuListEntry
import cc.mewcraft.wakame.item.resolveToItemWrapper
import cc.mewcraft.wakame.util.cooldown.Cooldown
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataCooldownKey
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import java.util.concurrent.TimeUnit

sealed class GovernmentListMenu(
    protected val viewer: Player,
) {

    companion object {
        private val KEY_TELEPORT_COOLDOWN = metadataCooldownKey("government_list_menu:teleport_cooldown")
    }

    protected abstract val uiSettings: BasicMenuSettings

    protected abstract fun getGovernments(): List<MenuListEntry>

    private val pagedGui = PagedGui.itemsBuilder()
        .setStructure(*uiSettings.structure)
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ ->
                    uiSettings.getIcon("background").resolveToItemWrapper()
                }
        )
        .addIngredient(
            '<', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page <= 0)
                        uiSettings.getIcon("background").resolveToItemWrapper()
                    else
                        uiSettings.getIcon("prev_page").resolveToItemWrapper {
                            standard {
                                component("current_page", Component.text(gui.page + 1))
                                component("total_page", Component.text(gui.pageCount))
                            }
                        }
                }
                .addClickHandler { _, gui, _ ->
                    gui.page -= 1
                }
        )
        .addIngredient(
            '>', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page >= gui.pageCount - 1) uiSettings.getIcon("background").resolveToItemWrapper()
                    else uiSettings.getIcon("next_page").resolveToItemWrapper {
                        standard {
                            component("current_page", Component.text(gui.page + 1))
                            component("total_page", Component.text(gui.pageCount))
                        }
                    }
                }
                .addClickHandler { _, gui, _ ->
                    gui.page += 1
                }
        )
        .addIngredient(
            '?', Item.builder()
                .setItemProvider { _ ->
                    uiSettings.getIcon("hint").resolveToItemWrapper()
                }
        )
        .setContent(buildContents())
        .build()

    private val window = Window.builder()
        .setUpperGui(pagedGui)
        .setTitle(uiSettings.title)
        .build(viewer)

    private fun buildContents(): List<Item> {
        val governments = getGovernments()
        val builtItems = governments
            .filter { gov -> gov.canShow }
            .mapIndexed { index, gov ->
                Item.builder()
                    .setItemProvider { _ ->
                        uiSettings.getIcon("entry").resolveToItemWrapper {
                            standard {
                                component("name", gov.name)
                                component("index", Component.text(index + 1))
                            }
                            folded("board", gov.board.map(Component::text))
                        }
                    }
                    .addClickHandler { _, click ->
                        val cooldown = click.player.metadata().getOrPut(KEY_TELEPORT_COOLDOWN) { Cooldown.of(5, TimeUnit.SECONDS) }
                        if (cooldown.test()) {
                            gov.teleport(click.player)
                        }
                    }
                    .build()
            }
        return builtItems
    }

    fun open() {
        window.open()
    }
}