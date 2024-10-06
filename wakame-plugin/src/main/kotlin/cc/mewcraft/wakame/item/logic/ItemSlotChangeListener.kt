package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.takeUnlessEmpty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// 写这么一个抽象类的原因:
// 根据物品的变化来触发的逻辑, 具有很多类似的地方.
// 例如, 都要检查 slots, 都要检查物品等级 ...
// 所以就写了个抽象类, 减少重复的逻辑.

/**
 * 代表一个监听物品在玩家背包里发生变化的逻辑.
 * 注意这里只负责监听, 而不应该去修改变化的结果.
 *
 * ### 逻辑和流程
 *
 * 各个抽象函数的执行逻辑如下 (标注为 `*` 是看情况执行):
 * 1. [onBegin] : 开始时执行的逻辑
 * 2. [handlePreviousItem]* : 处理旧物品的逻辑
 * 3. [handleCurrentItem]* : 处理新物品的逻辑
 * 4. [onEnd] : 结束时执行的逻辑
 *
 * 其中 [handlePreviousItem] 和 [handleCurrentItem]
 * 是否执行最终取决于 [test] 的返回值. 如果返回 `false`,
 * 则不执行; 反之则执行.
 *
 * 关于代码, 请看 [handleEvent].
 */
internal abstract class ItemSlotChangeListener {

    /**
     * 检查物品 [itemStack] 是否为需要处理的对象.
     *
     * @param player 涉及的玩家
     * @param slot 涉及的物品槽
     * @param itemStack 涉及的物品, 保证不为空气
     * @param nekoStack 对应的萌芽物品, 可能为 `null`
     */
    protected abstract fun test(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean

    /**
     * 流程最开始执行的逻辑.
     *
     * @param player 涉及的玩家
     */
    protected open fun onBegin(player: Player) = Unit

    /**
     * @param player 涉及的玩家
     * @param slot 涉及的物品槽
     * @param itemStack 涉及的物品, 保证不为空气
     * @param nekoStack 对应的萌芽物品, 可能为 `null`
     */
    protected abstract fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?)

    /**
     * @param player 涉及的玩家
     * @param slot 涉及的物品槽
     * @param itemStack 涉及的物品, 保证不为空气
     * @param nekoStack 对应的萌芽物品, 可能为 `null`
     */
    protected abstract fun handleCurrentItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?)

    /**
     * 流程结束时执行的逻辑.
     *
     * @param player 涉及的玩家
     */
    protected open fun onEnd(player: Player) = Unit

    /**
     * 检查物品槽.
     */
    protected fun testSlot(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        if (nekoStack == null) {
            return false
        }

        return nekoStack.slotGroup.contains(slot)
    }

    /**
     * 检查冒险等级.
     */
    protected fun testLevel(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        if (nekoStack == null) {
            return true // 如果不是萌芽物品, 那么玩家的等级一定高于该物品 (0)
        }

        val itemLevel = nekoStack.components.get(ItemComponentTypes.LEVEL)?.level
        if (itemLevel == null) {
            return true // 如果物品没有等级, 那么玩家的等级一定高于该物品 (0)
        }

        val playerLevel = player.toUser().level
        return itemLevel <= playerLevel
    }

    ///

    fun handleEvent(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val slot = event.slot
        val oldItemStack = event.oldItemStack?.takeUnlessEmpty()
        val newItemStack = event.newItemStack?.takeUnlessEmpty()

        if (oldItemStack == null && newItemStack == null) {
            return // will it ever happen?
        }

        val oldNekoStack = oldItemStack?.tryNekoStack
        val newNekoStack = newItemStack?.tryNekoStack
        if (oldNekoStack == null && newNekoStack == null) {
            return // fast-return
        }

        onBegin(player)

        if (oldItemStack != null && test(player, slot, oldItemStack, oldNekoStack)) {
            handlePreviousItem(player, slot, oldItemStack, oldNekoStack)
        }

        if (newItemStack != null && test(player, slot, newItemStack, newNekoStack)) {
            handleCurrentItem(player, slot, newItemStack, newNekoStack)
        }

        onEnd(player)
    }
}