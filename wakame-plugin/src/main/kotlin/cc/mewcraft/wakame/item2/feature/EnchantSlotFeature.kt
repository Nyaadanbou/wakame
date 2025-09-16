package cc.mewcraft.wakame.item2.feature

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entryOrElse
import cc.mewcraft.wakame.item2.*
import cc.mewcraft.wakame.item2.config.property.ItemPropTypes
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataTypes
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
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
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
@Init(stage = InitStage.POST_WORLD)
object EnchantSlotFeature : Listener {

    private val BASE_ENCHANTMENT_SLOT_PROVIDER: BaseSlotProviderType by MAIN_CONFIG.entryOrElse<BaseSlotProviderType>(BaseSlotProviderType.NONE, "base_enchantment_slot_provider")

    enum class BaseSlotProviderType {
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
     * 为物品设置额外的魔咒槽位数量.
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
     * 返回物品的魔咒槽位基本数量.
     */
    fun getSlotBase(item: ItemStack): Int {
        val amount = when (BASE_ENCHANTMENT_SLOT_PROVIDER) {
            BaseSlotProviderType.NONE -> Int.MIN_VALUE
            BaseSlotProviderType.PROP -> item.getProp(ItemPropertyTypes.ENCHANT_SLOT_BASE) ?: 0
            BaseSlotProviderType.RARITY -> item.getData(ItemDataTypes.RARITY)?.unwrap()?.enchantSlotBase ?: 0
        }
        return amount
    }

    /**
     * 返回物品的魔咒槽位额外数量.
     */
    fun getSlotExtra(item: ItemStack): Int {
        return item.getData(ItemDataTypes.EXTRA_ENCHANT_SLOTS) ?: 0
    }

    /**
     * 返回物品的魔咒槽位最大数量.
     */
    fun getSlotLimit(item: ItemStack): Int {
        return item.getData(ItemDataTypes.RARITY)?.unwrap()?.enchantSlotLimit ?: 0
    }

    /**
     * 返回物品的魔咒槽位总数量 (基础 + 额外).
     */
    fun getSlotTotal(item: ItemStack): Int {
        return getSlotBase(item) + getSlotExtra(item)
    }

    /**
     * 返回物品当前已使用的魔咒槽位数量.
     */
    fun getSlotUsed(item: ItemStack): Int {
        return getSlotCapacity(item, item.enchantments)
    }

    @EventHandler
    private fun on(event: InventoryClickEvent) {
        handleIntrinsicSlots(event)
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
        runTaskLater(1) { player.updateInventory() }

        // 发送提示信息
        player.sendMessage(TranslatableMessages.MSG_MAX_ENCHANT_SLOT_REACHED)
        player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)

        // 更新经验条
        player.giveExp(-1)
        if (player.totalExperience > 0) player.giveExp(1)
    }

    // 监听玩家在背包界面点击物品时, 根据配置和物品类型, 自动为物品设置默认的"魔咒槽"数量
    private fun handleIntrinsicSlots(event: InventoryClickEvent) {

    }

    // 监听玩家在背包中用"额外槽物品"对目标物品进行右键或左键操作时, 给目标物品增加"魔咒槽"数量, 并做各种校验和反馈
    private fun handleExtraSlots(event: InventoryClickEvent) {
        if (event.isCancelled) return
        if (!event.isRightClick && !event.isLeftClick) return
        val clickedInventory = event.clickedInventory as? PlayerInventory ?: return // 玩家必须点击自己背包里的物品进行操作
        val player = event.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.CREATIVE) return // 直接安静的忽略创造模式, 不发送任何提示信息
        val targetItem = event.currentItem?.takeUnlessEmpty() ?: return
        val extraItem = event.cursor.takeUnlessEmpty() ?: return

        val isExtraItem = extraItem.hasProp(ItemPropTypes.ENCHANT_SLOT_ADDER)
        if (!isExtraItem) return

        val slotLimit = getSlotLimit(targetItem)
        val slotTotal = getSlotTotal(targetItem)
        if (slotTotal >= slotLimit) {
            player.sendMessage(TranslatableMessages.MSG_MAX_ENCHANT_SLOT_REACHED)
            player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
            // 这里直接 return 而不取消事件, 因为玩家可能只是想 swap 这个物品
            return
        }

        if (slotLimit <= 0) return // 没有魔咒槽位, 直接安静的忽略

        event.isCancelled = true // 取消事件, 不然被安装额外槽位的物品会留在 cursor 上

        extraItem.subtract()
        addSlotExtra(targetItem)
        player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f)

        runTaskLater(1) { player.updateInventory() } // 必须延迟 1t 否则被打额外槽位的物品会从视觉上消失
    }

    // 监听玩家在魔咒台为物品魔咒时, 限制物品的魔咒数量, 防止超出自定义的最大"魔咒槽"数量
    @EventHandler
    private fun on(event: EnchantItemEvent) {
        val player = event.enchanter
        val item = event.item.takeUnlessEmpty() ?: return
        if (item.type == Material.BOOK) return // 书不受魔咒槽位限制, 因为书上
        val enchantsToAdd = event.enchantsToAdd
        val slotLimit = getSlotLimit(item)
        val slotUsedAlready = getSlotUsed(item)
        val slotUsedAfter = getSlotCapacity(item, enchantsToAdd)
        val slotUsedFinal = slotUsedAlready + slotUsedAfter

        if (slotUsedFinal <= slotLimit) {
            // 如果魔咒后没有超出最大容量, 则直接放行
            return
        }

        // 超出了最大容量, 取消事件并给出提示
        event.isCancelled = true
        player.sendMessage(TranslatableMessages.MSG_NO_FREE_ENCHANT_SLOTS)
        player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)

        // 随机移除超出容量的魔咒
        val excessAmt = slotUsedFinal - slotLimit
        if (item.type != Material.BOOK && excessAmt > 0) {
            runTask {
                var removedAmt = 0
                val enchantsOnItem = (item.getData(DataComponentTypes.ENCHANTMENTS)?.enchantments() ?: emptyMap()).toMutableMap()
                enchantsOnItem.forEach { (enchantment, _) ->
                    if (removedAmt >= excessAmt) return@forEach
                    enchantsOnItem.remove(enchantment)
                    removedAmt += getSlotCapacity(item, enchantment)
                }
                item.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(enchantsOnItem))
                player.sendMessage(TranslatableMessages.MSG_REMOVED_EXCESS_ENCHANTMENTS.arguments(Component.text(removedAmt)))
                player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
            }
        }

        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
    }

    // 监听玩家在锻造台 (Smithing Table) 合成物品时, 自动设置和校验物品的魔咒数量, 防止超出自定义的最大"魔咒槽"数量
    @EventHandler
    private fun on(event: SmithItemEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory
        val result = inventory.result?.takeUnlessEmpty() ?: return
        val slotLimit = getSlotLimit(result)
        val slotUsed = getSlotUsed(result)
        if (slotUsed > slotLimit) {
            event.isCancelled = true
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            player.sendMessage(TranslatableMessages.MSG_NO_FREE_ENCHANT_SLOTS)
            player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
            return
        }
    }
}