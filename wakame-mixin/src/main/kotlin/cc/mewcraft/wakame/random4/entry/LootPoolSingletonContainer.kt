package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.context.LootContext
import cc.mewcraft.wakame.random4.predicate.LootPredicate

abstract class LootPoolSingletonContainer<S>(
    val weight: Int,
    val quality: Int,
    conditions: List<LootPredicate>,
) : LootPoolEntryContainer<S>(conditions) {
    private val entry: LootPoolEntry<S> = object : EntryBase() {
        override fun createData(context: LootContext, dataConsumer: (S) -> Unit) {
            this@LootPoolSingletonContainer.createData(context, dataConsumer)
        }
    }

    protected abstract fun createData(context: LootContext, dataConsumer: (S) -> Unit)

    final override fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean {
        if (this.canRun(context)) {
            dataConsumer.invoke(this.entry)
            return true
        } else {
            return false
        }
    }

    protected abstract inner class EntryBase : LootPoolEntry<S> {
        override fun getWeight(luck: Float): Int {
            val qualityModifier = this@LootPoolSingletonContainer.quality.toFloat() * luck
            val baseWeight = (this@LootPoolSingletonContainer.weight + qualityModifier).toInt()
            return baseWeight
        }
    }
}