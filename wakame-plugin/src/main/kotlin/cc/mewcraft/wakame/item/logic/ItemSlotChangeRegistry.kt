package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.initializer.Initializable

/**
 * 储存了所有 [ItemSlotChangeListener] 实例.
 *
 * ### 实现指南
 * 如果代码需要监听玩家背包内的物品变化, 应该继承
 * [ItemSlotChangeListener] 然后将实例添加到
 * [ItemSlotChangeRegistry.listeners] 中.
 *
 * 注意不要重复添加同一个实例.
 */
internal object ItemSlotChangeRegistry : Initializable {
    /**
     * 当前所有已注册的 [ItemSlotChangeListener] 实例.
     *
     * 警告: 如果注册同一个实例多次, 将导致实例被多次调用.
     */
    val listeners: MutableList<ItemSlotChangeListener> = mutableListOf()

    override fun onPreWorld() {
        listeners += AttackSpeedItemSlotChangeListener
        listeners += AttributeItemSlotChangeListener
        listeners += EnchantmentItemSlotChangeListener
        listeners += KizamiItemSlotChangeListener
        listeners += SkillItemSlotChangeListener
    }
}
