package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2

class EmptyLoot(weight: Int, quality: Int, conditions: List<LootPredicate>) : LootPoolSingletonContainer<Nothing>(weight, quality, conditions) {
    companion object {
        val SERIALIZER: TypeSerializer2<EmptyLoot> = TypeSerializer2 { type, node ->
            val (weight, quality, conditions) = commonFields(node)
            EmptyLoot(weight, quality, conditions)
        }
    }

    override fun createData(context: LootContext, dataConsumer: (Nothing) -> Unit) = Unit
}