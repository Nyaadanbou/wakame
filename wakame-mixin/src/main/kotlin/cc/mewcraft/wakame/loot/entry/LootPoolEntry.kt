package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext

/**
 * 代表 [cc.mewcraft.wakame.loot.LootPool] 其中的条目.
 */
interface LootPoolEntry<S> {
    /**
     * 根据 [luck] 计算该条目被选中的权重.
     */
    fun getWeight(luck: Float): Int

    /**
     * 创建该条目的数据, 并将其传递给 [dataConsumer].
     */
    fun createData(context: LootContext, dataConsumer: (S) -> Unit)
}