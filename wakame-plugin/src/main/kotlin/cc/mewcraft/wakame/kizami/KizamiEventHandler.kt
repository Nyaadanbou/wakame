package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry.getBy
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 铭刻系统与事件系统的交互逻辑.
 */
class KizamiEventHandler {

    companion object {
        /**
         * 判断物品是否可以提供铭刻的谓词.
         */
        private val KIZAMI_PREDICATE = { item: NekoStack, slot: ItemSlot ->
            item.slotGroup.contains(slot) && item.templates.has(ItemTemplateTypes.KIZAMIABLE)
        }
    }

    /**
     * 当玩家装备的物品发生变化时, 执行的逻辑.
     */
    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        updateKizamiz(player, slot, oldItem, newItem)
    }

    /**
     * 根据旧物品和新物品所提供的铭刻, 更新玩家的铭刻.
     *
     * 这里的新旧物品指的是玩家先前“激活”的物品和当前“激活”的物品.
     *
     * 该函数本质上是更新铭刻给玩家提供的具体效果, 例如: 添加铭刻提供的属性, 移除铭刻提供的技能.
     *
     * @param player 玩家
     * @param slot 新旧物品发生变化时所在的槽位
     * @param oldItem 之前“激活”的物品; 如果为空气则应该传入 `null`
     * @param newItem 当前“激活”的物品; 如果为空气则应该传入 `null`
     */
    private fun updateKizamiz(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        val oldNekoStack = oldItem?.tryNekoStack
        val newNekoStack = newItem?.tryNekoStack

        // Optimization: if old item and new item are both null, we can fast return.
        if (oldNekoStack == null && newNekoStack == null) {
            return
        }

        val user = player.toUser()
        val kizamiMap = user.kizamiMap

        // First, remove all the existing kizami effects from the player,
        // since we will recalculate the kizami map and apply new kizami
        // effects (based on the new kizami map) to the player.
        kizamiMap.forEach { (kizami, amount) ->
            KizamiRegistry.EFFECTS.getBy(kizami, amount).remove(user)
        }

        // debug message - remove it when all done
        if (!kizamiMap.isEmpty()) {
            player.sendMessage("${Bukkit.getCurrentTick()} - 铭刻 (旧): " + kizamiMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }

        // Recalculate the kizami map:
        // subtract kizami amount, based on the old item,
        // then add kizami amount, based on the new item.
        shrinkKizamizAmount(oldNekoStack, slot, user)
        growKizamizAmount(newNekoStack, slot, user)

        // Apply the new kizami effects to the player,
        // based on the current amount of kizamiz.
        val it = kizamiMap.iterator()
        while (it.hasNext()) {
            val (kizami, amount) = it.next()
            if (amount > 0) {
                KizamiRegistry.EFFECTS.getBy(kizami, amount).apply(user)
            } else {
                it.remove()
            }
        }

        // debug message - remove it when all done
        if (!kizamiMap.isEmpty()) {
            player.sendMessage("${Bukkit.getCurrentTick()} - 铭刻 (新): " + kizamiMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }
    }

    /**
     * 将物品 [item] 提供的铭刻添加到 [user] 身上.
     *
     * @param item 可能提供铭刻的物品
     * @param slot 物品当前所在的槽位
     * @param user 要被增加铭刻的玩家
     */
    private fun growKizamizAmount(item: NekoStack?, slot: ItemSlot, user: User<Player>) {
        if (item == null) return
        val kizamiSet = getKizamiObjects(item, slot)
        val kizamiMap = user.kizamiMap
        kizamiMap.addOneEach(kizamiSet)
    }

    /**
     * 将物品 [item] 提供的铭刻从 [user] 身上移除.
     *
     * @param item 可能提供铭刻的物品
     * @param slot 物品当前所在的槽位
     * @param user 要被减少铭刻的玩家
     */
    private fun shrinkKizamizAmount(item: NekoStack?, slot: ItemSlot, user: User<Player>) {
        if (item == null) return
        val kizamiSet = getKizamiObjects(item, slot)
        val kizamiMap = user.kizamiMap
        kizamiMap.subtractOneEach(kizamiSet)
    }

    /**
     * 返回物品上的所有铭刻.
     *
     * @param item 可能提供铭刻的物品
     * @param slot 物品当前所在的槽位
     * @return 物品提供的铭刻
     */
    private fun getKizamiObjects(item: NekoStack, slot: ItemSlot): Set<Kizami> {
        if (!KIZAMI_PREDICATE(item, slot)) return emptySet()
        val itemKizamiz = item.components.get(ItemComponentTypes.KIZAMIZ) ?: return emptySet()
        return itemKizamiz.kizamiz
    }
}