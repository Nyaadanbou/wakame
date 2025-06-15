package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.context.LootContext
import cc.mewcraft.wakame.random4.predicate.LootPredicate
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class EmptyLoot(weight: Int, quality: Int, conditions: List<LootPredicate>) : LootPoolSingletonContainer<Nothing>(weight, quality, conditions) {
    override fun createData(context: LootContext, dataConsumer: (Nothing) -> Unit) = Unit
}