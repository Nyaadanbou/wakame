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

    /**
     * 当玩家装备的物品发生变化时, 执行的逻辑.
     */
    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        updateKizamiz(player, oldItem, newItem) {
            it.slotGroup.contains(slot) && it.templates.has(ItemTemplateTypes.KIZAMIABLE)
        }
    }

    /**
     * 根据玩家之前“激活”的物品和当前“激活”的物品所提供的属性, 更新玩家的铭刻.
     *
     * 这里的新/旧指的是玩家先前“激活”的物品和当前“激活”的物品.
     *
     * 该函数本质上是更新铭刻给玩家提供的具体效果, 例如: 添加铭刻提供的属性,
     * 移除铭刻提供的技能, 等等.
     *
     * @param player 玩家
     * @param oldItem 之前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param newItem 当前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param predicate 判断物品能否提供属性的谓词
     */
    private fun updateKizamiz(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: (NekoStack) -> Boolean,
    ) {
        val oldNekoStack = oldItem?.tryNekoStack
        val newNekoStack = newItem?.tryNekoStack

        // Optimization:
        // if old item and new item are both null,
        // we can fast return.
        if (oldNekoStack == null && newNekoStack == null) {
            return
        }

        val user = player.toUser()
        val kizamiMap = user.kizamiMap

        // First, remove all the existing kizami effects from the player,
        // since we will recalculate the kizami map and apply new kizami
        // effects (based on the new kizami map) to the player.
        val immutableAmountMap = kizamiMap.immutableAmountMap
        immutableAmountMap.forEach { (kizami, amount) ->
            KizamiRegistry.EFFECTS.getBy(kizami, amount).remove(kizami, user)
        }

        if (immutableAmountMap.isNotEmpty()) { // debug message - remove it when all done
            player.sendMessage("${Bukkit.getCurrentTick()} - your kizami (old): " + immutableAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }

        // Recalculate the kizami map.
        // The algorithm is simple:
        // subtract kizami amount, based on the old item,
        // then add kizami amount, based on the new item.
        oldNekoStack?.shrinkKizamiz(user, predicate)
        newNekoStack?.growKizamiz(user, predicate)

        val mutableAmountMap = kizamiMap.mutableAmountMap
        val iterator = mutableAmountMap.iterator()
        while (iterator.hasNext()) {
            val (kizami, amount) = iterator.next()
            if (amount > 0) {
                // apply new kizami effects to the player
                KizamiRegistry.EFFECTS.getBy(kizami, amount).apply(kizami, user)
            } else {
                // optimization: remove kizami with zero and negative amount, so it won't be iterated again
                iterator.remove()
            }
        }

        if (mutableAmountMap.isNotEmpty()) { // debug message - remove it when all done
            player.sendMessage("${Bukkit.getCurrentTick()} - your kizami (new): " + mutableAmountMap.map { "${it.key.uniqueId}: ${it.value}" }.joinToString(", "))
        }
    }

    /**
     * 将该物品提供的铭刻添加到 [user] 身上.
     *
     * @param user 要移除铭刻的玩家
     * @param predicate 判断物品能否提供铭刻的谓词
     * @receiver 可能提供铭刻的物品
     */
    private fun NekoStack.growKizamiz(user: User<Player>, predicate: (NekoStack) -> Boolean) {
        val kizamiSet = this.getKizamiSet(predicate)
        val kizamiMap = user.kizamiMap
        kizamiMap.addOneEach(kizamiSet)
    }

    /**
     * 将该物品提供的铭刻从 [user] 身上移除.
     *
     * @param user 要移除铭刻的玩家
     * @param predicate 判断物品能否提供铭刻的谓词
     * @receiver 可能提供铭刻的物品
     */
    private fun NekoStack.shrinkKizamiz(user: User<Player>, predicate: (NekoStack) -> Boolean) {
        val kizamiSet = this.getKizamiSet(predicate)
        val kizamiMap = user.kizamiMap
        kizamiMap.subtractOneEach(kizamiSet)
    }

    /**
     * 根据谓词的结果, 返回物品上的所有铭刻.
     *
     * @param predicate 判断物品能否提供铭刻的谓词
     * @return 物品提供的铭刻
     * @receiver 可能提供铭刻的物品
     */
    private fun NekoStack.getKizamiSet(predicate: (NekoStack) -> Boolean): Set<Kizami> {
        if (!predicate(this)) {
            return emptySet()
        }
        val itemKizamiz = this.components.get(ItemComponentTypes.KIZAMIZ) ?: return emptySet()
        return itemKizamiz.kizamiz
    }
}