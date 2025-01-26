package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage

/**
 * 储存了所有 [ItemSlotChangeEventListener] 实例.
 *
 * ### 实现指南
 * 如果代码需要监听玩家背包内的物品变化, 应该继承
 * [ItemSlotChangeEventListener] 然后将实例添加到
 * [ItemSlotChangeEventListenerRegistry.listeners] 中.
 *
 * 注意不要重复添加同一个实例.
 */
@Init(
    stage = InitStage.PRE_WORLD
)
internal object ItemSlotChangeEventListenerRegistry {

    /**
     * 当前所有已注册的 [ItemSlotChangeEventListener] 实例.
     *
     * 警告: 如果注册同一个实例多次, 将导致实例被多次调用.
     */
    val listeners: MutableList<ItemSlotChangeEventListener> = mutableListOf()

    @InitFun
    fun init() {
        listeners += AttackSpeedItemSlotChangeListener
        listeners += AttributeItemSlotChangeListener
        listeners += EnchantmentItemSlotChangeListener
        listeners += KizamiItemSlotChangeListener
        listeners += AbilityItemSlotChangeListener
    }
}
