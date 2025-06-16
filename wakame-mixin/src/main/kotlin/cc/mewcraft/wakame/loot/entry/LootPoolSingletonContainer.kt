package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

abstract class LootPoolSingletonContainer<S>(
    val weight: Int,
    val quality: Int,
    conditions: List<LootPredicate>,
) : LootPoolEntryContainer<S>(conditions) {

    companion object {
        protected fun commonFields(node: ConfigurationNode): Triple<Int, Int, List<LootPredicate>> {
            val weight = node.node("weight").get<Int>(0)
            val quality = node.node("quality").get<Int>(0)
            val conditions = node.node("conditions").get<List<LootPredicate>>() ?: emptyList()
            return Triple(weight, quality, conditions)
        }
    }

    @Transient
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