package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate

class EmptyLoot(weight: Int, quality: Int, conditions: List<LootPredicate>) : LootPoolSingletonContainer<Nothing>(weight, quality, conditions) {
    companion object {
        val SERIALIZER: SimpleSerializer<EmptyLoot> = SimpleSerializer { type, node ->
            val (weight, quality, conditions) = commonFields(node)
            EmptyLoot(weight, quality, conditions)
        }
    }

    override fun createData(context: LootContext, dataConsumer: (Nothing) -> Unit) = Unit
}