package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.typeTokenOf

object LootPredicates {
    @JvmField
    val INVERTED: LootPredicateType<InvertedPredicate> = register("inverted", InvertedPredicate.SERIALIZER)

    @JvmField
    val ANY_OF: LootPredicateType<AnyOfPredicate> = register("any_of", AnyOfPredicate.SERIALIZER)

    @JvmField
    val ALL_OF: LootPredicateType<AllOfPredicate> = register("all_of", AllOfPredicate.SERIALIZER)

    private inline fun <reified T : LootPredicate> register(name: String, serializer: TypeSerializer2<T>): LootPredicateType<T> {
        val type = LootPredicateType.create(typeTokenOf<T>(), serializer)
        return Registry.register(BuiltInRegistries.LOOT_PREDICATE_TYPE, Identifiers.of(name), type)
    }
}
