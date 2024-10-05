package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.initializer.Initializable

/**
 * 储存了所有 [ItemSlotChangeListener] 实例.
 *
 * 所有需要监听物品在玩家背包里发生变化的机制, 都应该 [register].
 */
internal object ItemSlotChangeRegistry: Initializable {
    private val listeners: MutableList<ItemSlotChangeListener> = mutableListOf()

    /**
     * 获取所有 [ItemSlotChangeListener] 实例.
     */
    fun listeners(): List<ItemSlotChangeListener> {
        return listeners
    }

    /**
     * 注册一个 [ItemSlotChangeListener] 实例.
     * 警告: 多次注册会导致多次调用.
     */
    fun register(listener: ItemSlotChangeListener) {
        listeners.add(listener)
    }

    override fun onPreWorld() {
        register(AttackSpeedItemSlotChangeListener)
        register(AttributeItemSlotChangeListener)
        register(EnchantmentItemSlotChangeListener)
        register(KizamiItemSlotChangeListener)
        register(SkillItemSlotChangeListener)
    }
}
