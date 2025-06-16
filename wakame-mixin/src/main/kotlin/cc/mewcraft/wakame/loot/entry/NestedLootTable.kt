package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require

class NestedLootTable<S>(
    val contents: RegistryEntry<LootTable<S>>,
    weight: Int,
    quality: Int,
    conditions: List<LootPredicate>,
) : LootPoolSingletonContainer<S>(weight, quality, conditions) {
    companion object {
        val SERIALIZER: TypeSerializer2<NestedLootTable<*>> = TypeSerializer2 { type, node ->
            val (weight, quality, conditions) = commonFields(node)
            val contents = node.node("contents").require<RegistryEntry<LootTable<Any>>>()
            NestedLootTable(contents, weight, quality, conditions)
        }
    }

    override fun createData(context: LootContext, dataConsumer: (S) -> Unit) {
        val lootTable = contents.unwrap()
        for (pool in lootTable.pools) {
            pool.addRandomItems(context, dataConsumer)
        }
    }
}