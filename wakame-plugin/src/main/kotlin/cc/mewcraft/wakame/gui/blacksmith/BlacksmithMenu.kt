package cc.mewcraft.wakame.gui.blacksmith

import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStation
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.recycle.*
import cc.mewcraft.wakame.reforge.repair.*
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
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
import xyz.xenondevs.invui.window.changeTitle
import xyz.xenondevs.invui.window.type.context.setTitle

internal class BlacksmithMenu(
    val station: BlacksmithStation,
    val viewer: Player,
) : KoinComponent, Listener {

    companion object Shared {
        private const val RECYCLING_INVENTORY_SIZE = 10
        private val BACKGROUND_ITEM = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).edit { hideTooltip = true }
    }

    fun open() {
        window2.open()
    }

    val recyclingStation: RecyclingStation
        get() = station.recyclingStation
    val repairingTable: RepairingTable
        get() = station.repairingTable

    val recyclingSession: RecyclingSession = SimpleRecyclingSession(recyclingStation, viewer, RECYCLING_INVENTORY_SIZE)
    val repairingSession: RepairingSession = SimpleRepairingSession(repairingTable, viewer)

    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.RECYCLE)

    var confirmed: Boolean = false

    private val playerInventory: PlayerInventory
        get() = viewer.inventory

    // 监听玩家与自己背包发生的交互
    @EventHandler
    private fun on(event: InventoryClickEvent) {
        // 当玩家点击自己背包里的物品时,
        // 先看当前“激活”的菜单是不是回收菜单;
        // 如果是, 则尝试将物品加入回收列表.
        // 如果不是, 则直接取消事件.

        if (event.result == Event.Result.DENY) {
            // 被取消基本意味着 InvUI 已经处理过了,
            // 我们这里直接返回不再做额外的处理.
            return
        }

        if (
            getCurrentTab() == TabType.RECYCLING &&
            event.action == InventoryAction.PICKUP_ALL &&
            event.isLeftClick
        ) {
            // 尝试将物品加入回收列表
            val playerSlot = event.slot
            val currentItem = event.currentItem /* 这是个 mirror */ ?: return
            val claimResult = recyclingSession.claimItem(currentItem, playerSlot)

            if (claimResult is RecyclingSession.ClaimResult.Failure) {
                // 如果未能成功加入回收列表, 则向玩家发送失败信息.
                // 本次交互后, 整个菜单里的物品也不需要发生变化.
                claimResult.description.forEach { viewer.sendMessage(it.color(NamedTextColor.RED)) }
                event.result = Event.Result.DENY
                return
            }

            if (claimResult is RecyclingSession.ClaimResult.Success) {
                // 如果成功加入回收列表, 则将物品从玩家背包里移除.
                // 这里移除的逻辑是“隐藏”物品堆叠, 而不是将其移除.

                // 让物品在玩家背包里消失.
                // 注意这里物品本身 (currentItem) 没有“消失”,
                // 只是把玩家当前点击的物品格设置为了空气.
                event.currentItem = null

                // 让物品显示在回收列表里.
                syncRecyclingInventory()

                // 刷新确认回收的图标.
                // 这里会显示回收的价格信息.
                sellButton.notifyWindows()
            }
        } else {
            event.result = Event.Result.DENY
        }
    }

    @EventHandler
    private fun on(event: PlayerAttemptPickupItemEvent) {
        event.isCancelled = true
    }

    // 用于展示玩家当前要回收的物品.
    // 将出现在整个菜单的上半部分.
    private val recyclingInventory: VirtualInventory = VirtualInventory(RECYCLING_INVENTORY_SIZE).apply {
        setPreUpdateHandler(::onRecyclingInventoryPreUpdate)
    }

    // 根据 session *重写* 菜单的所有物品
    private fun syncRecyclingInventory() {
        // 获取底层数组
        val unsafeItems = recyclingInventory.unsafeItems

        // 清空菜单里原本的物品,
        // 这里直接操作 unsafeItems 以节约一点性能.
        unsafeItems.fill(null)

        // 重新向菜单里添加物品,
        // 这里直接操作 unsafeItems 以节约一点性能.
        recyclingSession.getAllClaims()
            .map { claim -> claim.originalItem }
            .toTypedArray()
            .copyInto(unsafeItems)

        // 操作 unsafeItems 需要手动调用 notifyWindows
        recyclingInventory.notifyWindows()
    }

    private fun onRecyclingInventoryPreUpdate(event: ItemPreUpdateEvent) {
        confirmed = false

        when {
            event.isAdd -> event.isCancelled = true
            event.isSwap -> event.isCancelled = true
            event.isRemove -> {
                event.isCancelled = true

                val slot = event.slot
                val unclaimed = recyclingSession.removeClaim(slot) ?: error("Claim not found for display slot $slot. This is a bug!")

                syncRecyclingInventory()

                // 物品从待回收列表移除, 玩家背包需要更新
                playerInventory.setItem(unclaimed.playerSlot, unclaimed.originalItem)
            }
        }
    }

    // [确认回收] 的按钮
    private val sellButton = SellButton()

    private val recyclingGui = Gui.normal { builder ->
        builder.setStructure(
            "i i i i i . .",
            "i i i i i . x",
        )
        builder.addIngredient('.', BACKGROUND_ITEM)
        builder.addIngredient('i', recyclingInventory)
        builder.addIngredient('x', sellButton)
    }

    private val repairingInventory: VirtualInventory = VirtualInventory(5).apply {
        setPreUpdateHandler(::onRepairingInventoryPreUpdate)
    }

    // 根据 session *重写* 菜单的所有物品
    private fun syncRepairingInventory() {
        val unsafeItems = repairingInventory.unsafeItems

        // 清空菜单里原本的物品
        unsafeItems.fill(null)

        // 重新向菜单里添加物品
        for (claim in repairingSession.getAllClaims()) {
            val displaySlot = claim.displaySlot
            val renderedItem = claim.originalItem // TODO #227 渲染物品: 耐久度, 修理费用, 交互提示(点击维修)
            if (displaySlot >= unsafeItems.size) {
                break
            }

            unsafeItems[displaySlot] = renderedItem
        }

        // 更新菜单物品
        repairingInventory.notifyWindows()
    }

    private fun onRepairingInventoryPreUpdate(e: ItemPreUpdateEvent) {
        confirmed = false

        when {
            e.isAdd -> {
                e.isCancelled = true
            }

            e.isSwap -> {
                e.isCancelled = true
            }

            e.isRemove -> {
                e.isCancelled = true

                val slot = e.slot
                val claim = repairingSession.getClaim(slot) ?: error("Claim not found for slot $slot. This is a bug!")
                if (claim.repairCost.test(viewer)) {
                    claim.repairCost.take(viewer)
                    claim.repair(viewer)

                    // 修复物品后从 claims 列表中移除
                    repairingSession.removeClaim(slot)

                    // 将菜单里的物品与 session 同步
                    syncRepairingInventory()

                } else {
                    viewer.sendMessage(text("无法承担修理费用!").color(NamedTextColor.RED))
                }
            }
        }
    }

    private val repairingGui = Gui.normal { builder ->
        builder.setStructure(
            "i i i i i . .",
            "* * * * * . .",
        )
        builder.addIngredient('.', BACKGROUND_ITEM)
        builder.addIngredient('*', ItemStack.of(Material.GRAY_STAINED_GLASS_PANE))
        builder.addIngredient('i', repairingInventory)
    }

    // 用于承载回收和修复两个 TabGui
    private val primaryUpperGui = TabGui.normal { builder ->
        builder.setStructure(
            ". . . . . . . . .",
            ". . * * * * * * *",
            "s . * * * * * * *",
            ". . . . . . . . .",
        )
        builder.addIngredient('.', BACKGROUND_ITEM)
        builder.addIngredient('*', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('s', SwitchItem())
        builder.setTabs(
            listOf(recyclingGui, repairingGui)
        )
    }

    private fun getCurrentTab(): TabType {
        return TabType.entries[primaryUpperGui.currentTab]
    }

    private val window2 = Window.single { builder ->
        builder.setGui(primaryUpperGui)
        builder.setTitle(recyclingStation.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    private fun onWindowOpen() {
        // 注册监听器
        registerEvents()

        logger.info("Opened blacksmith menu for ${viewer.name}")
    }

    private fun onWindowClose() {
        // 取消监听器
        unregisterEvents()

        logger.info("Closed blacksmith menu for ${viewer.name}")

        // 将回收列表里的物品返回给玩家
        recyclingSession.getAllClaims().forEach { claim -> playerInventory.setItem(claim.playerSlot, claim.originalItem) }
    }

    private inner class SwitchItem : ControlItem<TabGui>() {
        override fun getItemProvider(gui: TabGui): ItemProvider {
            val currentTab = getCurrentTab()
            val itemStack = if (currentTab == TabType.RECYCLING) {
                window2.changeTitle(recyclingStation.title)
                ItemStack.of(Material.ANVIL).edit { itemName = text("切换到物品修复") }
            } else {
                window2.changeTitle(repairingTable.title)
                ItemStack.of(Material.EMERALD).edit { itemName = text("切换到物品回收") }
            }
            return ItemWrapper(itemStack)
        }

        // 切换 tab 时的逻辑
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            confirmed = false

            val currentTab = getCurrentTab()
            gui.setTab(if (currentTab == TabType.RECYCLING) 1 else 0)

            // 刷新修理会话
            repairingSession.registerClaims(playerInventory)

            // 将回收列表中的所有物品放回玩家背包
            recyclingSession.getAllClaims().forEach { claim -> playerInventory.setItem(claim.playerSlot, claim.originalItem) }
            // 然后将回收列表中的所有 claim 清除
            recyclingSession.reset()

            // 将数据同步到菜单
            syncRepairingInventory()
            syncRecyclingInventory()
        }
    }

    private inner class SellButton : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            // TODO #227 渲染图标:
            //  你正在出售
            //  <empty>
            //  - item 1
            //  - item 2
            //  <empty>
            //  价值: x-y
            //  <empty>
            //  点击确认
            //  ------
            //  确认出售
            //  <dark_gray>接下来要出售
            //  <empty>
            //  - item 1
            //  - item 2
            //  <empty>
            //  价值: x-y
            //  <empty>
            //  再次点击确认

            val itemStack = ItemStack.of(Material.WRITABLE_BOOK)
            itemStack.edit {
                itemName = text(
                    if (confirmed)
                        "确认出售"
                    else
                        "你正在出售"
                )
            }
            return ItemWrapper(itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (!confirmed) {
                confirmed = true
                notifyWindows()
                return
            }

            confirmed = false

            // 购买玩家要回收的物品
            when (
                val purchaseResult = recyclingSession.purchase(false)
            ) {
                is RecyclingSession.PurchaseResult.Failure -> {
                    // 如果购买失败, 则向玩家发送失败信息.
                    purchaseResult.description.forEach { viewer.sendMessage(it.color(NamedTextColor.RED)) }
                }

                is RecyclingSession.PurchaseResult.Success -> {
                    // 如果购买成功, 则向玩家发送成功信息.
                    purchaseResult.description.forEach { viewer.sendMessage(it) }

                    // 同步菜单里的回收列表
                    syncRecyclingInventory()

                    // 更新本图标
                    notifyWindows()
                }
            }
        }
    }

    private enum class TabType {
        RECYCLING, REPAIRING,
    }
}