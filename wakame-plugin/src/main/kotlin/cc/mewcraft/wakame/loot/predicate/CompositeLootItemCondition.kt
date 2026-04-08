package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.loot.context.LootContext

abstract class CompositeLootItemCondition(
    protected val terms: List<LootPredicate>,
    private val composedPredicate: (LootContext) -> Boolean,
) : LootPredicate {
    companion object {
        @JvmStatic
        protected fun <T : CompositeLootItemCondition> createSerializer(factory: (List<LootPredicate>) -> T): SimpleSerializer<T> {
            return SimpleSerializer { type, node ->
                val terms = node.node("terms").require<List<LootPredicate>>()
                factory(terms)
            }
        }
    }

    final override fun invoke(context: LootContext): Boolean {
        return this.composedPredicate.invoke(context)
    }
}
