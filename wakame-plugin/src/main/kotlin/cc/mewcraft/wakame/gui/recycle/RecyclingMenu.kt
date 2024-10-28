@file:OptIn(ExperimentalContracts::class)

package cc.mewcraft.wakame.gui.recycle

import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.recycle.*
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class RecyclingMenu(
    val station: RecyclingStation,
    val viewer: Player,
) : KoinComponent {

    companion object Shared {
        private val BACKGROUND_ITEM = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)
    }

    fun open() {
        window.open()
    }

    val session: RecyclingSession = SimpleRecyclingSession()

    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.RECYCLE)

    var confirmed: Boolean = false

    private val recyclingInputSlot: VirtualInventory = VirtualInventory(10).apply {
        setPreUpdateHandler(::onRecyclingInputPreUpdate)
    }

    private fun onRecyclingInputPreUpdate(e: ItemPreUpdateEvent) {
        confirmed = false

        when {
            e.isAdd -> {}

            e.isSwap -> {
                e.isCancelled = true
            }

            e.isRemove -> {
                e.isCancelled = true
                recyclingInputSlot.setItemSilently(e.slot, null)
                val prevItem = e.previousItem!!
                viewer.inventory.addItem(prevItem)
            }
        }
    }

    private val repairingInputSlot2: VirtualInventory = VirtualInventory(10).apply {
        setPreUpdateHandler(::onRepairingInputPreUpdate)
    }

    private fun isRepairable(itemStack: ItemStack?): Boolean {
        contract { returns(true) implies (itemStack != null) }
        return itemStack?.isDamaged == true
    }

    private fun onRepairingInputPreUpdate(e: ItemPreUpdateEvent) {
        confirmed = false

        when {
            e.isAdd -> {}

            e.isSwap -> {
                e.isCancelled = true
            }

            e.isRemove -> {}
        }
    }

    private val recyclingGui = Gui.normal { builder ->
        builder.setStructure(
            "i i i i i . .",
            "i i i i i . x",
        )
        builder.addIngredient('.', BACKGROUND_ITEM)
        builder.addIngredient('i', recyclingInputSlot)
        builder.addIngredient('x', SellItem())
    }

    private val repairingGui = Gui.normal { builder ->
        builder.setStructure(
            "i i i i i . .",
            "i i i i i . x",
        )
        builder.addIngredient('.', BACKGROUND_ITEM)
        builder.addIngredient('i', repairingInputSlot2)
        builder.addIngredient('x', RepairItem())
    }

    private val mainGui = TabGui.normal { builder ->
        builder.setStructure(
            ". . . . . . . . .",
            ". . * * * * * * *",
            "s . * * * * * * *",
            ". . . . . . . . .",
        )
        builder.addIngredient('.', BACKGROUND_ITEM)
        builder.addIngredient('*', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('s', SwitchItem())
        builder.setTabs(listOf(recyclingGui, repairingGui))
    }

    private val window = Window.single { builder ->
        builder.setGui(mainGui)
        builder.setTitle(station.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    private fun onWindowOpen() {
        logger.info("Opened recycling station for ${viewer.name}")
    }

    private fun onWindowClose() {
        logger.info("Closed recycling station for ${viewer.name}")

        // TODO 将 input 里的所有物品归还给玩家
        viewer.inventory.addItem(*recyclingInputSlot.unsafeItems.filterNotNull().toTypedArray())
        viewer.inventory.addItem(*repairingInputSlot2.unsafeItems.filterNotNull().toTypedArray())
    }

    private inner class SwitchItem : ControlItem<TabGui>() {
        override fun getItemProvider(gui: TabGui): ItemProvider {
            val currentTab = TabType.entries[gui.currentTab]
            val itemStack = if (currentTab == TabType.RECYCLING) {
                ItemStack.of(Material.ANVIL).edit { itemName = Component.text("切换到物品修复") }
            } else {
                ItemStack.of(Material.EMERALD).edit { itemName = Component.text("切换到物品回收") }
            }
            return ItemWrapper(itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val currentTab = TabType.entries[gui.currentTab]
            gui.setTab(if (currentTab == TabType.RECYCLING) 1 else 0)
        }
    }

    private inner class RepairItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val itemStack = ItemStack.of(Material.WRITABLE_BOOK)
            itemStack.edit { itemName = Component.text("确认修复") }
            return ItemWrapper(itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (!confirmed) {
                confirmed = true
                return
            }

            repairingInputSlot2.unsafeItems.forEach { item ->
                if (isRepairable(item)) {
                    item.damage = 0
                }
            }

            repairingInputSlot2.notifyWindows()

            player.sendMessage("修复!")
        }
    }

    private inner class SellItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val itemStack = ItemStack.of(Material.WRITABLE_BOOK)
            itemStack.edit { itemName = Component.text("确认出售") }
            return ItemWrapper(itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (!confirmed) {
                confirmed = true
                return
            }

            player.sendMessage("出售!")
        }
    }

    private enum class TabType {
        RECYCLING, REPAIRING,
    }
}