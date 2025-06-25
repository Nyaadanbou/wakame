package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require

abstract class CompositeLootItemCondition(
    protected val terms: List<LootPredicate>,
    private val composedPredicate: (LootContext) -> Boolean,
): LootPredicate {
    companion object {
        @JvmStatic
        protected fun <T : CompositeLootItemCondition> createSerializer(factory: (List<LootPredicate>) -> T): TypeSerializer2<T> {
            return TypeSerializer2 { type, node ->
                val terms = node.node("terms").require<List<LootPredicate>>()
                factory(terms)
            }
        }
    }

    final override fun invoke(context: LootContext): Boolean {
        return this.composedPredicate.invoke(context)
    }
}
