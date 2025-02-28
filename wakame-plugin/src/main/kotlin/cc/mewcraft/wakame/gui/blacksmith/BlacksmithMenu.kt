package cc.mewcraft.wakame.gui.blacksmith

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.repairing_table.RepairingTableItemRendererContext
import cc.mewcraft.wakame.item.extension.level
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.lang.translate
import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStation
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.reforge.recycle.RecyclingSession
import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.recycle.SimpleRecyclingSession
import cc.mewcraft.wakame.reforge.repair.RepairingSession
import cc.mewcraft.wakame.reforge.repair.RepairingTable
import cc.mewcraft.wakame.reforge.repair.SimpleRepairingSession
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.itemName
import cc.mewcraft.wakame.util.item.itemNameOrType
import cc.mewcraft.wakame.util.item.maxDamage
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.TranslationArgument
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.inventory.PlayerInventory
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
) : Listener {

    fun open() {
        window2.open()
        viewer.sendMessage(TranslatableMessages.MSG_OPENED_BLACKSMITH_MENU)
    }

    val recyclingStation: RecyclingStation
        get() = station.recyclingStation
    val repairingTable: RepairingTable
        get() = station.repairingTable

    val recyclingSession: RecyclingSession = SimpleRecyclingSession(recyclingStation, viewer, station.recyclingInventorySize)
    val repairingSession: RepairingSession = SimpleRepairingSession(repairingTable, viewer)

    val logger: Logger = LOGGER.decorate(prefix = ReforgingStationConstants.RECYCLING_LOG_PREFIX)

    var confirmed: Boolean = false

    private val playerInventory: PlayerInventory
        get() = viewer.inventory

    // 监听玩家与自己背包发生的交互
    @EventHandler(
        ignoreCancelled = true // 被取消基本意味着 InvUI 已经处理过了
    )
    private fun on(event: InventoryClickEvent) {
        // 当玩家点击自己背包里的物品时,
        // 先看当前“激活”的菜单是不是回收菜单;
        // 如果是, 则尝试将物品加入回收列表.
        // 如果不是, 则直接取消事件.

        if (event.whoClicked != viewer) {
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
                when (claimResult.reason) {
                    RecyclingSession.ClaimResult.Failure.Reason.TOO_MANY_CLAIMS -> viewer.sendMessage(TranslatableMessages.MSG_ERR_FULL_RECYCLING_STASH_LIST)
                    RecyclingSession.ClaimResult.Failure.Reason.UNSUPPORTED_ITEM -> viewer.sendMessage(TranslatableMessages.MSG_ERR_UNSUPPORTED_RECYCLING_ITEM_TYPE)
                }
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

    @EventHandler(ignoreCancelled = true)
    private fun on(event: PlayerAttemptPickupItemEvent) {
        if (event.player != viewer) {
            return
        }

        event.isCancelled = true
    }

    // 用于展示玩家当前要回收的物品.
    // 将出现在整个菜单的上半部分.
    private val recyclingInventory: VirtualInventory = VirtualInventory(station.recyclingInventorySize).apply {
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
                val unclaimed = recyclingSession.removeClaim(slot) ?: error("claim not found for display slot $slot. This is a bug!")

                syncRecyclingInventory()

                // 重新渲染出售按钮
                sellButton.notifyWindows()

                // 物品从待回收列表移除, 玩家背包需要更新
                playerInventory.setItem(unclaimed.playerSlot, unclaimed.originalItem)
            }
        }
    }

    // [确认回收] 的按钮
    private val sellButton = SellButton()

    private val recyclingGui = Gui.normal { builder ->
        builder.setStructure(*station.recyclingMenuSettings.structure)
        builder.addIngredient('.', station.recyclingMenuSettings.getSlotDisplay("background").resolveToItemStack())
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
            val displayItem = claim.originalItem.clone().apply {
                val context = RepairingTableItemRendererContext(
                    damage = this.damage,
                    maxDamage = this.maxDamage,
                    repairCost = claim.repairCost.value,
                )
                ItemRenderers.REPAIRING_TABLE.render(this, context)
            }

            if (displaySlot >= unsafeItems.size) {
                break
            }

            unsafeItems[displaySlot] = displayItem
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
                val claim = repairingSession.getClaim(slot) ?: error("claim not found for slot $slot. This is a bug!")
                if (claim.repairCost.test(viewer)) {
                    claim.repairCost.take(viewer)
                    claim.repair(viewer)

                    // 发送消息
                    viewer.sendMessage(
                        TranslatableMessages.MSG_SPENT_X_REPAIRING_ITEM.arguments(
                            TranslationArgument.numeric(claim.repairCost.value),
                            TranslationArgument.component(claim.originalItem.itemName ?: translatable(claim.originalItem))
                        )
                    )

                    // 修复物品后从 claims 列表中移除
                    repairingSession.removeClaim(slot)

                    // 将菜单里的物品与 session 同步
                    syncRepairingInventory()

                } else {
                    viewer.sendMessage(TranslatableMessages.MSG_ERR_NOT_ENOUGH_MONEY_TO_REPAIR_ITEM)
                }
            }
        }
    }

    private val repairingGui = Gui.normal { builder ->
        builder.setStructure(*station.repairingMenuSettings.structure)
        builder.addIngredient('.', station.repairingMenuSettings.getSlotDisplay("background").resolveToItemStack())
        builder.addIngredient('*', station.repairingMenuSettings.getSlotDisplay("background2").resolveToItemStack())
        builder.addIngredient('i', repairingInventory)
    }

    // 用于承载回收和修复两个 TabGui
    private val primaryUpperGui = TabGui.normal { builder ->
        builder.setStructure(*station.primaryMenuSettings.structure)
        builder.addIngredient('.', station.primaryMenuSettings.getSlotDisplay("background").resolveToItemStack())
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
        builder.setTitle(station.recyclingMenuSettings.title)
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
                window2.changeTitle(station.recyclingMenuSettings.title)
                station.primaryMenuSettings.getSlotDisplay("select_repairing").resolveToItemStack()
            } else {
                window2.changeTitle(station.repairingMenuSettings.title)
                station.primaryMenuSettings.getSlotDisplay("select_recycling").resolveToItemStack()
            }
            return ItemWrapper(itemStack)
        }

        // 切换 tab 时的逻辑
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            confirmed = false

            val currentTab = getCurrentTab()
            gui.setTab(if (currentTab == TabType.RECYCLING) 1 else 0)

            // 将回收列表中的所有物品放回玩家背包
            recyclingSession.getAllClaims().forEach { claim -> playerInventory.setItem(claim.playerSlot, claim.originalItem) }
            // 然后将回收列表中的所有 claim 清除
            recyclingSession.reset()

            // 刷新修理会话
            repairingSession.registerClaims(playerInventory)

            // 将数据同步到菜单
            syncRepairingInventory()
            syncRecyclingInventory()
        }
    }

    private inner class SellButton : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            if (recyclingSession.getAllClaims().isEmpty()) {
                return station.recyclingMenuSettings.getSlotDisplay("recycle_when_empty").resolveToItemWrapper()
            }

            val purchaseResult = recyclingSession.purchase(true)
            val itemWrapper = when (purchaseResult) {
                is RecyclingSession.PurchaseResult.Failure -> {
                    station.recyclingMenuSettings.getSlotDisplay("recycle_when_error").resolveToItemWrapper()
                }

                is RecyclingSession.PurchaseResult.Success -> {
                    val slotDisplayId = if (confirmed) "recycle_when_confirmed" else "recycle_when_unconfirmed"
                    station.recyclingMenuSettings.getSlotDisplay(slotDisplayId).resolveToItemWrapper {
                        standard {
                            component(
                                "total_worth", TranslatableMessages.MSG_BLACKSMITH_TOTAL_WORTH.arguments(
                                    TranslationArgument.numeric(purchaseResult.minPrice),
                                    TranslationArgument.numeric(purchaseResult.maxPrice)
                                ).translate(viewer)
                            )
                        }
                        folded("item_list") {
                            for (item in recyclingSession.getAllClaims().map { claim -> claim.originalItem }) {
                                val itemName = item.itemNameOrType
                                val itemLevel = item.wrap()?.level?.let(::text)
                                if (itemLevel != null) {
                                    resolve("with_level") {
                                        component("item_name", itemName)
                                        component("item_level", itemLevel)
                                    }
                                } else {
                                    resolve("without_level") {
                                        component("item_name", itemName)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> error("unexpected purchase result: $purchaseResult")
            }

            return itemWrapper
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
                is RecyclingSession.PurchaseResult.Empty -> {
                    // 如果没有物品, 则什么也不做.
                }

                is RecyclingSession.PurchaseResult.Failure -> {
                    // 如果购买失败, 则向玩家发送失败信息.
                    viewer.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR)
                }

                is RecyclingSession.PurchaseResult.Success -> {
                    // 如果购买成功, 则向玩家发送成功信息.
                    viewer.sendMessage(
                        TranslatableMessages.MSG_SOLD_ITEMS_FOR_X_COINS.arguments(
                            TranslationArgument.numeric(purchaseResult.fixPrice)
                        )
                    )

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