package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2

/**
 * 若满足指定条件, 从 [children] 中选择第一个满足条件的子项.
 */
class AlternativesEntry<S>(
    children: List<LootPoolEntryContainer<S>>,
    conditions: List<LootPredicate>,
) : CompositeEntryBase<S>(children, conditions) {
    companion object {
        val SERIALIZER: TypeSerializer2<AlternativesEntry<*>> = makeSerializer(::AlternativesEntry)
    }

    override fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S> {
        return when (children.size) {
            0 -> ComposableEntryContainer.alwaysFalse()
            1 -> children[0]
            2 -> children[0].or(children[1])
            else -> ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
                for (composableEntryContainer in children) {
                    if (context.isIterating) {
                        // If we are iterating, we expand all composable entries
                        composableEntryContainer.expand(context, dataConsumer)
                        continue
                    }
                    if (composableEntryContainer.expand(context, dataConsumer)) {
                        return@ComposableEntryContainer true
                    }
                }
                false
            }
        }
    }
}