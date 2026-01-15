package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.registry.entry.RegistryEntry

class NestedLootTable<S>(
    val contents: RegistryEntry<LootTable<S>>,
    weight: Int,
    quality: Int,
    conditions: List<LootPredicate>,
) : LootPoolSingletonContainer<S>(weight, quality, conditions) {
    companion object {
        val SERIALIZER: SimpleSerializer<NestedLootTable<*>> = SimpleSerializer { type, node ->
            val (weight, quality, conditions) = commonFields(node)
            val contents = node.node("contents").require<RegistryEntry<LootTable<Any>>>()
            NestedLootTable(contents, weight, quality, conditions)
        }
    }

    override fun createData(context: LootContext, dataConsumer: (S) -> Unit) {
        val lootTable = contents.unwrap()
        for (pool in lootTable.pools) {
            pool.addRandomItems(context, dataConsumer = dataConsumer)
        }
    }
}