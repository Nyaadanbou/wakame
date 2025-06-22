package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Predicates

class AllOfPredicate(
    terms: List<LootPredicate>,
) : CompositeLootItemCondition(terms, Predicates.allOf(terms)) {
    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<AllOfPredicate> = createSerializer(::AllOfPredicate)
    }
}
