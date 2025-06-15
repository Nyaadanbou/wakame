package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.context.LootContext

interface LootPoolEntry<S> {
    fun getWeight(luck: Float): Int

    fun createData(context: LootContext, dataConsumer: (S) -> Unit)
}