package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Predicates

class AnyOfPredicate(
    terms: List<LootPredicate>,
) : CompositeLootItemCondition(terms, Predicates.anyOf(terms)) {
    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<AnyOfPredicate> = createSerializer(::AnyOfPredicate)
    }
}
