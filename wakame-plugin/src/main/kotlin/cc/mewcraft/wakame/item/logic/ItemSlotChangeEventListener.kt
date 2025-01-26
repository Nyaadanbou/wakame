package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.takeUnlessEmpty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// 写这么一个抽象类的原因:
// 根据物品的变化来触发的逻辑, 具有很多类似的地方.
// 例如, 都要检查 slots, 都要检查物品等级 ...
// 所以就写了个抽象类, 减少重复的逻辑.

/**
 * 代表一个监听玩家背包里物品发生的变化的逻辑.
 * 这里只负责监听, 而不应该去修改物品变化的结果.
 *
 * ### 逻辑和流程
 *
 * 各个抽象函数的执行顺序如下 (标注为 `*` 是看情况执行):
 * 1. [onBegin] : 最开始执行的逻辑
 * 2. [handlePreviousItem]* : 处理变化之前的物品的逻辑
 * 3. [handleCurrentItem]* : 处理变化之后的物品的逻辑
 * 4. [onEnd] : 结束时执行的逻辑
 *
 * 其中 [handlePreviousItem] 和 [handleCurrentItem]
 * 是否执行最终取决于 [test] 的返回值. 如果返回 `false`,
 * 则不执行; 反之则执行.
 *
 * 关于以上流程的默认实现, 请看 [handleEvent].
 */
internal abstract class ItemSlotChangeEventListener {

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
     * 处理变化之前的物品.
     *
     * 通常是把物品效果从玩家身上 *移除* 的逻辑.
     *
     * @param player 涉及的玩家
     * @param slot 涉及的物品槽
     * @param itemStack 涉及的物品, 保证不为空气
     * @param nekoStack 对应的萌芽物品, 可能为 `null`
     */
    protected abstract fun handlePreviousItem(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?)

    /**
     * 处理变化之后的物品.
     *
     * 通常是把物品效果 *添加* 到玩家身上的逻辑.
     *
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
     * 检查物品 [itemStack] 是否在“正确”的物品槽.
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

    protected fun testDurability(player: Player, slot: ItemSlot, itemStack: ItemStack, nekoStack: NekoStack?): Boolean {
        if (nekoStack == null) {
            return true // 如果不是萌芽物品, 那么该物品应该按照游戏原版的逻辑处理
        }

        if (!nekoStack.isDamageable) {
            return true // 如果物品有“无法破坏”或耐久组件不完整, 那么认为物品没有耐久度, 应该返回 true
        }

        if (nekoStack.damage >= nekoStack.maxDamage) {
            return false // 如果物品已经损坏, 那么应该返回 false
        }

        return true
    }

    ///

    // /**
    //  * 用于在特殊时机强制更新物品提供给玩家的效果.
    //  */
    // fun forceUpdate(player: Player) {
    //     val everyItemSlot = ItemSlotRegistry.all()
    //     for (itemSlot in everyItemSlot) {
    //         val itemStack = itemSlot.getItem(player) ?: continue
    //         val nekoStack = itemStack.shadowNeko()
    //         if (test(player, itemSlot, itemStack, nekoStack)) {
    //             onBegin(player)
    //
    //             // 这里我们对同一个物品进行两次操作:
    //             // 先从玩家身上移除物品“自己”的效果,
    //             // 然后再把物品的效果添加到玩家身上.
    //             handlePreviousItem(player, itemSlot, itemStack, nekoStack)
    //             handleCurrentItem(player, itemSlot, itemStack, nekoStack)
    //
    //             onEnd(player)
    //         }
    //     }
    // }

    /**
     * 用于正常处理 [PlayerItemSlotChangeEvent].
     */
    fun handleEvent(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val slot = event.slot
        val oldItemStack = event.oldItemStack?.takeUnlessEmpty()
        val newItemStack = event.newItemStack?.takeUnlessEmpty()

        if (oldItemStack == null && newItemStack == null) {
            return // will it ever happen?
        }

        val oldNekoStack = oldItemStack?.shadowNeko()
        val newNekoStack = newItemStack?.shadowNeko()

        onBegin(player)

        if (oldItemStack != null &&
            testSlot(player, slot, oldItemStack, oldNekoStack) // 移除物品效果时, 只检查 item slot 是否满足
        ) {
            // 移除物品效果时, 只检查 item slot 是否满足.
            // 这样可以保证在一些特殊情况下 (例如强制降低冒险等级),
            // 玩家的物品效果仍然能够被正确移除.
            handlePreviousItem(player, slot, oldItemStack, oldNekoStack)
        }

        if (newItemStack != null && test(player, slot, newItemStack, newNekoStack)) {
            handleCurrentItem(player, slot, newItemStack, newNekoStack)
        }

        onEnd(player)
    }
}