package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext

interface LootPoolEntry<S> {
    fun getWeight(luck: Float): Int

    fun createData(context: LootContext, dataConsumer: (S) -> Unit)
}