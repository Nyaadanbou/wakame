package cc.mewcraft.wakame.gui.blacksmith

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.HotfixItemName
import cc.mewcraft.wakame.item.display.ItemRenderers
import cc.mewcraft.wakame.item.display.implementation.repairing_table.RepairingTableItemRendererContext
import cc.mewcraft.wakame.item.extension.level
import cc.mewcraft.wakame.item.resolveToItemWrapper
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
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.inventory.PlayerInventory
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window

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
        // 当前激活的是回收菜单 → 尝试将物品加入回收列表
        // 否则直接取消事件
        if (event.whoClicked != viewer) {
            return
        }

        if (
            getCurrentTab() == TabType.RECYCLING &&
            event.action == InventoryAction.PICKUP_ALL &&
            event.isLeftClick
        ) {
            val playerSlot = event.slot
            val currentItem = event.currentItem /* mirror */ ?: return
            val claimResult = recyclingSession.claimItem(currentItem, playerSlot)

            if (claimResult is RecyclingSession.ClaimResult.Failure) {
                // 加入回收列表失败, 发送提示
                when (claimResult.reason) {
                    RecyclingSession.ClaimResult.Failure.Reason.TOO_MANY_CLAIMS -> viewer.sendMessage(TranslatableMessages.MSG_ERR_FULL_RECYCLING_STASH_LIST)
                    RecyclingSession.ClaimResult.Failure.Reason.UNSUPPORTED_ITEM -> viewer.sendMessage(TranslatableMessages.MSG_ERR_UNSUPPORTED_RECYCLING_ITEM_TYPE)
                }
                event.result = Event.Result.DENY
                return
            }

            if (claimResult is RecyclingSession.ClaimResult.Success) {
                // 将物品从玩家背包"隐藏"(设为空气), 显示到回收列表中
                event.currentItem = null
                syncRecyclingInventory()
                sellButton.notifyWindows() // 刷新回收价格信息
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

    // 展示玩家当前要回收的物品, 出现在菜单上半部分
    private val recyclingInventory: VirtualInventory = VirtualInventory(station.recyclingInventorySize).apply {
        addPreUpdateHandler(::onRecyclingInventoryPreUpdate)
    }

    // 根据 session 重写菜单中的所有物品
    private fun syncRecyclingInventory() {
        val unsafeItems = recyclingInventory.unsafeItems
        unsafeItems.fill(null) // 直接操作 unsafeItems 以提升性能
        recyclingSession.getAllClaims()
            .map { claim -> claim.originalItem }
            .toTypedArray()
            .copyInto(unsafeItems)
        recyclingInventory.notifyWindows() // 操作 unsafeItems 后需手动通知
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
                sellButton.notifyWindows() // 重新渲染出售按钮
                playerInventory.setItem(unclaimed.playerSlot, unclaimed.originalItem) // 物品归还玩家背包
            }
        }
    }

    // [确认回收] 的按钮
    private val sellButton: Item = Item.builder()
        .setItemProvider provider@{ _ ->
            if (recyclingSession.getAllClaims().isEmpty()) {
                return@provider station.recyclingMenuSettings.getIcon("recycle_when_empty").resolveToItemWrapper()
            }

            when (val purchaseResult = recyclingSession.purchase(true)) {
                is RecyclingSession.PurchaseResult.Failure -> {
                    station.recyclingMenuSettings.getIcon("recycle_when_error").resolveToItemWrapper()
                }

                is RecyclingSession.PurchaseResult.Success -> {
                    val slotDisplayId = if (confirmed) "recycle_when_confirmed" else "recycle_when_unconfirmed"
                    station.recyclingMenuSettings.getIcon(slotDisplayId).resolveToItemWrapper {
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
                                val itemName = HotfixItemName.getItemName(item) ?: item.itemNameOrType
                                val itemLevel = item.level?.level?.let(::text)
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
        }
        .addClickHandler handler@{ item, click ->
            if (!confirmed) {
                confirmed = true
                item.notifyWindows()
                return@handler
            }

            confirmed = false

            when (
                val purchaseResult = recyclingSession.purchase(false)
            ) {
                is RecyclingSession.PurchaseResult.Empty -> {
                    // 如果没有物品, 则什么也不做.
                }

                is RecyclingSession.PurchaseResult.Failure -> {
                    viewer.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR)
                }

                is RecyclingSession.PurchaseResult.Success -> {
                    viewer.sendMessage(
                        TranslatableMessages.MSG_SOLD_ITEMS_FOR_X_COINS.arguments(
                            TranslationArgument.numeric(purchaseResult.fixPrice)
                        ).translate(viewer)
                    )
                    syncRecyclingInventory()
                    item.notifyWindows()
                }
            }
        }
        .build()

    private val recyclingGui = Gui.builder()
        .setStructure(*station.recyclingMenuSettings.structure)
        .addIngredient('.', station.recyclingMenuSettings.getIcon("background").resolveToItemStack())
        .addIngredient('i', recyclingInventory)
        .addIngredient('x', sellButton)
        .build()

    private val repairingInventory: VirtualInventory = VirtualInventory(5).apply {
        addPreUpdateHandler(::onRepairingInventoryPreUpdate)
    }

    // 根据 session 重写菜单中的修理物品
    private fun syncRepairingInventory() {
        val unsafeItems = repairingInventory.unsafeItems
        unsafeItems.fill(null)

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

                    // 发送修理扣费消息
                    viewer.sendMessage(
                        TranslatableMessages.MSG_SPENT_X_REPAIRING_ITEM.arguments(
                            TranslationArgument.numeric(claim.repairCost.value),
                            TranslationArgument.component(HotfixItemName.getItemName(claim.originalItem) ?: claim.originalItem.itemName ?: translatable(claim.originalItem))
                        ).translate(viewer)
                    )

                    repairingSession.removeClaim(slot) // 修复后移除 claim
                    syncRepairingInventory() // 同步菜单

                } else {
                    viewer.sendMessage(TranslatableMessages.MSG_ERR_NOT_ENOUGH_MONEY_TO_REPAIR_ITEM)
                }
            }
        }
    }

    private val repairingGui = Gui.builder()
        .setStructure(*station.repairingMenuSettings.structure)
        .addIngredient('.', station.repairingMenuSettings.getIcon("background").resolveToItemStack())
        .addIngredient('*', station.repairingMenuSettings.getIcon("background2").resolveToItemStack())
        .addIngredient('i', repairingInventory)
        .build()

    private val switchItem = BoundItem.tabBuilder()
        .setItemProvider { _, gui ->
            val currentTab = TabType.entries[gui.tab]
            val itemStack = if (currentTab == TabType.RECYCLING) {
                window2.setTitle(station.recyclingMenuSettings.title)
                station.primaryMenuSettings.getIcon("select_repairing").resolveToItemStack()
            } else {
                window2.setTitle(station.repairingMenuSettings.title)
                station.primaryMenuSettings.getIcon("select_recycling").resolveToItemStack()
            }
            ItemWrapper(itemStack)
        }
        .addClickHandler { _, gui, _ ->
            confirmed = false

            val currentTab = TabType.entries[gui.tab]
            gui.tab = if (currentTab == TabType.RECYCLING) 1 else 0

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
        .build()

    // 用于承载回收和修复两个 TabGui
    private val primaryUpperGui: TabGui = TabGui.builder()
        .setStructure(*station.primaryMenuSettings.structure)
        .addIngredient('.', station.primaryMenuSettings.getIcon("background").resolveToItemStack())
        .addIngredient('*', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient('s', switchItem)
        .setTabs(
            listOf(recyclingGui, repairingGui)
        )
        .build()

    private fun getCurrentTab(): TabType {
        return TabType.entries[primaryUpperGui.tab]
    }

    private val window2: Window = Window.builder()
        .setUpperGui(primaryUpperGui)
        .setTitle(station.recyclingMenuSettings.title)
        .setViewer(viewer)
        .addOpenHandler(::onWindowOpen)
        .addCloseHandler { onWindowClose() }
        .build()

    private fun onWindowOpen() {
        registerEvents() // 注册监听器
        logger.info("Opened blacksmith menu for ${viewer.name}")
    }

    private fun onWindowClose() {
        unregisterEvents() // 取消监听器
        logger.info("Closed blacksmith menu for ${viewer.name}")
        // 将回收列表里的物品返回给玩家
        recyclingSession.getAllClaims().forEach { claim -> playerInventory.setItem(claim.playerSlot, claim.originalItem) }
    }

    private enum class TabType {
        RECYCLING, REPAIRING,
    }
}