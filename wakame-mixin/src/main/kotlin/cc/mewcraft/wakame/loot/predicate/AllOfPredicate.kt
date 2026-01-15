package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.util.Predicates

class AllOfPredicate(
    terms: List<LootPredicate>,
) : CompositeLootItemCondition(terms, Predicates.allOf(terms)) {
    companion object {
        @JvmField
        val SERIALIZER: SimpleSerializer<AllOfPredicate> = createSerializer(::AllOfPredicate)
    }
}
