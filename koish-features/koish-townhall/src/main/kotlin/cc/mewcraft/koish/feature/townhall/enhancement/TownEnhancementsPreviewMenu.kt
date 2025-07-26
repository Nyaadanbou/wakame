package cc.mewcraft.koish.feature.townhall.enhancement

import cc.mewcraft.koish.feature.townhall.bridge.koishify
import cc.mewcraft.koish.feature.townhall.component.TownHall
import cc.mewcraft.koish.feature.townhall.data.DummyTownDataStorage
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.hook.impl.towny.bridge.koishify
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
import cc.mewcraft.wakame.hook.impl.towny.data.DataEnhancement
import cc.mewcraft.wakame.hook.impl.towny.data.DummyTownDataStorage
import cc.mewcraft.wakame.item2.display.resolveToItemWrapper
import cc.mewcraft.wakame.util.Identifiers
import com.palmergames.bukkit.towny.`object`.Town
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.builder.setLore
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.TabItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

internal class TownEnhancementsPreviewMenu(
    val previous: TownEnhancementsMenu,
    val enhancement: DataEnhancement,
) {
    private val town: Town
        get() = previous.town

    private val player: Player
        get() = previous.viewer

    private val settings: BasicMenuSettings
        get() = BasicMenuSettings(
            title = Component.text("TownEnhancements"),
            structure = arrayOf(
                ". . . u . . . . .",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                ". . . . . . . . ."
            ),
            icons = hashMapOf(
                "background" to Identifiers.of("internal/menu/common/default/background"),
                "prev_page" to Identifiers.of("internal/menu/enhancement/prev_page"),
                "next_page" to Identifiers.of("internal/menu/enhancement/next_page"),
                "content_list_slot_horizontal" to Identifiers.of("internal/menu/enhancement/content_list_slot_horizontal")
            )
        )

    private val upgradeSetting: BasicMenuSettings
        get() = BasicMenuSettings(
            title = Component.text("TownEnhancements"),
            structure = arrayOf(
                ". . . . . . . . .",
                ". . . . u . . . .",
                ". . . . . . . . .",
            ),
            icons = hashMapOf(
                "background" to Identifiers.of("internal/towny/enhancement/background"),
                "prev_page" to Identifiers.of("internal/towny/enhancement/prev_page"),
                "next_page" to Identifiers.of("internal/towny/enhancement/next_page"),
                "content_list_slot_horizontal" to Identifiers.of("internal/towny/enhancement/content_list_slot_horizontal")
            )
        )

    /**
     * 城镇增强菜单的 [Gui], 用于预览当前增强的效果.
     *
     * - `.`: background
     * - `x`: gui content
     */
    private val primaryUpgradeGui: Gui = Gui.normal { builder ->
        builder.setStructure(*upgradeSetting.structure)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('u', UpgradeItem())
    }

    /**
     * 城镇增强菜单的 [Gui].
     *
     * - `.`: background
     * - `u`: upgrade
     * - `x`: gui content
     */
    private val primaryGui: TabGui = TabGui.normal { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('u', UpgradeTab(0))

        builder.setTabs(listOf(primaryUpgradeGui))
    }

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setViewer(player)
        builder.setTitle(settings.title)
    }

    fun open() {
        player.closeInventory()
        primaryWindow.open()
        player.sendMessage("Opening preview for enhancement: ${enhancement.type} at level ${enhancement.level}")
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

    inner class UpgradeTab(private val tab: Int) : TabItem(tab) {
        override fun getItemProvider(gui: TabGui): ItemProvider {
            return if (gui.currentTab == tab) {
                ItemBuilder(Material.GLOWSTONE_DUST)
                    .setDisplayName("Upgrade (selected)")
            } else {
                ItemBuilder(Material.GUNPOWDER)
                    .setDisplayName("Upgrade (not selected)")
            }
        }
    }

    inner class UpgradeItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemBuilder(Material.GLOWSTONE_DUST)
                .setDisplayName("Upgrade")
                .setLore(
                    listOf(
                        Component.text("Enhancement Type: ${enhancement.type}"),
                        Component.text("Current Level: ${enhancement.level}"),
                        Component.text("Needs: 20 level"),
                        Component.text(
                            "Click to upgrade this enhancement."
                        )
                    )
                ).also { it.amount = enhancement.level }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (!clickType.isLeftClick)
                return
            if (player.level < 20) {
                player.sendMessage("You need at least 20 levels to upgrade this enhancement.")
                return
            }
            upgradeEnhancement()
            player.level -= 20
            player.playSound(Sound.sound().type(Key.key("entity.player.levelup")).build(), Sound.Emitter.self())
            player.closeInventory()
        }

        private fun upgradeEnhancement() {
            val townEntity = town.koishify()
            with(Fleks.INSTANCE.world) {
                townEntity[TownHall].enhancements[enhancement.type]?.let { enhancementEntity ->
                    val currentLevel = enhancementEntity[Level].level
                    if (currentLevel < 5) {
                        enhancementEntity[Level].level = currentLevel + 1
                        DummyTownDataStorage.saveTownHall(townEntity.unwrap())
                        player.sendMessage("Successfully upgraded ${enhancement.type} to level ${currentLevel + 1}.")
                    } else {
                        player.sendMessage("This enhancement is already at the maximum level.")
                    }
                } ?: run {
                    player.sendMessage("This enhancement does not exist in your town hall.")
                }
            }
        }
    }
}