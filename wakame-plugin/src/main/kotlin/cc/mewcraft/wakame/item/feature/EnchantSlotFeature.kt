package cc.mewcraft.wakame.item.feature

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.extension.rarity2
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.runTask
import cc.mewcraft.wakame.util.runTaskLater
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.event.inventory.SmithItemEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.view.AnvilView

/**
 * 实现了魔咒槽位机制.
 */
// FIXME 这功能不太能用 ItemBehavior 实现, 因为在一次事件中涉及到多个物品, 具体原因类似我们如何实现属性机制一样
//  但从包含与否的关系来看, 单独写个监听器(类似这里的 xxxFeature) 实际上是包含了 ItemBehavior 的所有功能的
//  我在考虑要不要把 ItemBehavior 干掉换成这种实现得了
@Init(InitStage.POST_WORLD)
object EnchantSlotFeature : Listener {

    private val ENCHANT_SLOT_BASE_PROVIDER: SlotBaseProviderType by MAIN_CONFIG.entryOrElse<SlotBaseProviderType>(SlotBaseProviderType.NONE, "enchant_slot_base_provider")

    enum class SlotBaseProviderType {
        NONE,
        PROP,
        RARITY
    }

    init {
        registerEvents()
    }

    /**
     * 返回 [enchantment] 在 [itemstack] 上所占用的槽位容量. 默认占用 1 个容量.
     */
    fun getSlotCapacity(itemstack: ItemStack, enchantment: Enchantment): Int {
        val capacityConfig = itemstack.getProp(ItemPropTypes.ENCHANT_SLOT_CAPACITY) ?: return 1
        val capacity = capacityConfig.getCapacity(enchantment)
        return capacity
    }

    /**
     * 返回 [enchantments] 在 [itemstack] 上所占用的槽位容量总和. 默认每个魔咒占用 1 个容量.
     */
    fun getSlotCapacity(itemstack: ItemStack, enchantments: Map<Enchantment, Int>): Int {
        return enchantments.keys.sumOf { enchantment -> getSlotCapacity(itemstack, enchantment) }
    }

    /**
     * 为物品设置额外的魔咒槽位容量.
     */
    fun setSlotExtra(item: ItemStack, amount: Int) {
        require(amount >= 0) { "amount must be greater than 0" }
        if (amount == 0) {
            item.removeData(ItemDataTypes.EXTRA_ENCHANT_SLOTS)
        } else {
            item.setData(ItemDataTypes.EXTRA_ENCHANT_SLOTS, amount)
        }
    }

    /**
     * 为物品增加一个魔咒槽位.
     *
     * @return 如果成功增加了一个魔咒槽位, 则返回 true; 否则返回 false (例如物品不支持魔咒槽位, 或者已经达到最大魔咒槽位)
     */
    fun addSlotExtra(item: ItemStack): Boolean {
        item.setData(ItemDataTypes.EXTRA_ENCHANT_SLOTS, getSlotExtra(item) + 1)
        return false
    }

    /**
     * 返回物品的魔咒槽位基本容量.
     */
    fun getSlotBase(item: ItemStack): Int {
        val amount = when (ENCHANT_SLOT_BASE_PROVIDER) {
            SlotBaseProviderType.NONE -> Int.MIN_VALUE
            SlotBaseProviderType.PROP -> item.getProp(ItemPropTypes.ENCHANT_SLOT_BASE) ?: 0
            SlotBaseProviderType.RARITY -> item.rarity2?.unwrap()?.enchantSlotBase ?: 0
        }
        return amount
    }

    /**
     * 返回物品的魔咒槽位额外容量.
     */
    fun getSlotExtra(item: ItemStack): Int {
        return item.getData(ItemDataTypes.EXTRA_ENCHANT_SLOTS) ?: 0
    }

    /**
     * 返回物品的魔咒槽位最大容量.
     */
    fun getSlotLimit(item: ItemStack): Int {
        return item.rarity2?.unwrap()?.enchantSlotLimit ?: 0
    }

    /**
     * 返回物品的魔咒槽位总容量 (基础 + 额外).
     */
    fun getSlotTotal(item: ItemStack): Int {
        return getSlotBase(item) + getSlotExtra(item)
    }

    /**
     * 返回物品当前已使用的魔咒槽位容量.
     */
    fun getSlotUsed(item: ItemStack): Int {
        return getSlotCapacity(item, item.enchantments)
    }

    @EventHandler
    private fun on(event: InventoryClickEvent) {
        handleExtraSlots(event)
        handleAnvil(event)
    }

    // 监听玩家在铁砧界面进行物品合成时的行为, 并根据自定义的"魔咒槽"逻辑进行处理
    private fun handleAnvil(event: InventoryClickEvent) {
        val inventory = event.inventory as? AnvilInventory ?: return
        val inventoryView = event.view as? AnvilView ?: return
        val player = event.whoClicked as? Player ?: return
        val item = inventory.getItem(0) ?: return
        val result = inventory.getItem(2) ?: return

        val limit = getSlotTotal(item)
        if (limit <= 0) return // 没有魔咒槽位 - 安静忽略
        val used = getSlotUsed(result)

        if (used <= limit) return // 已占用的槽位数量未超出最大容量 - 安静忽略

        event.isCancelled = true
        inventoryView.repairCost = 0

        // 关闭背包
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)

        // 更新背包 (延迟 1t 总没错)
        runTaskLater(1) { -> player.updateInventory() }

        // 发送提示信息
        player.sendMessage(TranslatableMessages.MSG_MAX_ENCHANT_SLOT_REACHED)
        player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)

        // 更新经验条
        player.giveExp(-1)
        if (player.totalExperience > 0) player.giveExp(1)
    }

    // 监听玩家在背包中用"额外槽物品"对目标物品进行右键或左键操作时, 给目标物品增加"魔咒槽"容量, 并做各种校验和反馈
    private fun handleExtraSlots(event: InventoryClickEvent) {
        if (event.isCancelled) return
        if (!event.isRightClick && !event.isLeftClick) return
        val clickedInventory = event.clickedInventory as? PlayerInventory ?: return // 玩家必须点击自己背包里的物品进行操作
        val player = event.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.CREATIVE) return // 直接安静的忽略创造模式, 不发送任何提示信息
        val extraItem = event.cursor.takeUnlessEmpty() ?: return
        val targetItem = event.currentItem?.takeUnlessEmpty() ?: return
        if (!extraItem.hasProp(ItemPropTypes.ENCHANT_SLOT_ADDER)) return
        if (targetItem.isExactKoish.not()) {
            // 如果不是非套皮 Koish 物品, 则不允许安装额外槽位
            // 这是因为物品系统的一大约定就是不修改原版物品堆叠

            // 对于可以附魔的物品, 这里我们需要给出提示信息, 消除玩家对此类物品无法添加额外槽位而产生的疑惑
            if (targetItem.hasData(DataComponentTypes.ENCHANTABLE)) {
                player.sendMessage(TranslatableMessages.MSG_ERR_CANNOT_ADD_EXTRA_ENCHANT_SLOT_FOR_VANILLA_ITEMS)
                player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
                event.isCancelled = true
            }

            return
        }
        val slotLimit = getSlotLimit(targetItem)
        val slotTotal = getSlotTotal(targetItem)
        if (slotTotal >= slotLimit) {
            // 这里直接 return 而不取消事件, 因为玩家可能只是想 swap 这个物品
            player.sendMessage(TranslatableMessages.MSG_MAX_ENCHANT_SLOT_REACHED)
            player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
            return
        }
        if (slotLimit <= 0) return // 没有魔咒槽位, 直接安静的忽略

        extraItem.subtract()
        addSlotExtra(targetItem)
        player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f)
        event.isCancelled = true // 取消事件, 不然被安装额外槽位的物品会留在 cursor 上

        runTaskLater(1) { -> player.updateInventory() } // 必须延迟 1t 否则被打额外槽位的物品会从视觉上消失
    }

    // 监听玩家在魔咒台为物品魔咒时, 限制物品的魔咒容量, 防止超出自定义的最大"魔咒槽"容量
    @EventHandler
    private fun on(event: EnchantItemEvent) {
        val player = event.enchanter
        val item = event.item.takeUnlessEmpty() ?: return
        if (item.type == Material.BOOK) return // 书不受魔咒槽位限制, 因为书没有附魔槽位的概念
        val enchantsToAdd = event.enchantsToAdd
        val slotTotal = getSlotTotal(item)
        val slotUsedAlready = getSlotUsed(item)
        val slotUsedAfter = getSlotCapacity(item, enchantsToAdd)
        val slotUsedFinal = slotUsedAlready + slotUsedAfter

        if (slotUsedFinal <= slotTotal) {
            // 如果魔咒后没有超出最大容量, 则直接放行
            return
        }

        // 超出了最大容量, 取消事件并给出提示
        player.sendMessage(TranslatableMessages.MSG_NO_FREE_ENCHANT_SLOTS)
        player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)

        // 随机移除超出容量的魔咒
        val excessAmount = slotUsedFinal - slotTotal
        if (excessAmount > 0) {
            runTask { ->
                var removedAmount = 0
                val enchantments = (item.getData(DataComponentTypes.ENCHANTMENTS)?.enchantments()?.entries ?: mutableListOf())
                    .sortedByDescending { (enchant, level) -> getSlotCapacity(item, enchant) } // 优先移除占用槽位容量较大的魔咒, 以最大程度保留物品上的魔咒
                    .toMutableList()
                val enchantmentsIt = enchantments.iterator()
                for ((enchant, level) in enchantmentsIt) {
                    if (removedAmount >= excessAmount) break
                    enchantmentsIt.remove()
                    removedAmount += getSlotCapacity(item, enchant)
                }
                val newEnchantments = ItemEnchantments.itemEnchantments(enchantments.associate { (enchant, level) -> enchant to level })
                item.setData(DataComponentTypes.ENCHANTMENTS, newEnchantments)
                player.sendMessage(TranslatableMessages.MSG_REMOVED_EXCESS_ENCHANTMENTS.arguments(Component.text(removedAmount)))
                player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
            }
        }
    }

    // 监听玩家在锻造台 (Smithing Table) 合成物品时, 自动设置和校验物品的魔咒容量, 防止超出自定义的最大"魔咒槽"容量
    @EventHandler(priority = EventPriority.LOWEST)
    private fun on(event: SmithItemEvent) {
        if (event.isCancelled) return

        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory

        val secondItem = inventory.inputEquipment?.takeUnlessEmpty() ?: return
        val resultItem = inventory.result?.takeUnlessEmpty() ?: return

        // 计算 result 物品的槽位容量 (= result 物品的基础槽位容量 + second 物品的额外槽位容量)
        val slotLimit = getSlotTotal(resultItem)
        val slotUsed = getSlotUsed(resultItem)
        if (slotUsed > slotLimit) {
            event.isCancelled = true
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            player.sendMessage(TranslatableMessages.MSG_NO_FREE_ENCHANT_SLOTS)
            player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun on(event: PrepareSmithingEvent) {
        val inventory = event.inventory

        val secondItem = inventory.inputEquipment?.takeUnlessEmpty() ?: return
        val resultItem = event.result?.takeUnlessEmpty() ?: return

        // 将 second slot 物品的额外槽位信息传递给 result 物品
        val extraEnchantSlotOnSecondItem = secondItem.getData(ItemDataTypes.EXTRA_ENCHANT_SLOTS)
        if (extraEnchantSlotOnSecondItem != null) {
            setSlotExtra(resultItem, extraEnchantSlotOnSecondItem)
            event.result = resultItem
        }
    }
}