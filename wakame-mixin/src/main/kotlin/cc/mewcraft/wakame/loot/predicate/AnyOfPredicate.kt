package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.util.Predicates

class AnyOfPredicate(
    terms: List<LootPredicate>,
) : CompositeLootItemCondition(terms, Predicates.anyOf(terms)) {
    companion object {
        @JvmField
        val SERIALIZER: SimpleSerializer<AnyOfPredicate> = createSerializer(::AnyOfPredicate)
    }
}
