package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate

/**
 * 若满足指定条件, 从 [children] 开头开始, 逐一选择子项直到有一个子项不满足条件为止.
 */
class SequentialEntry<S>(
    children: List<LootPoolEntryContainer<S>> = emptyList(),
    conditions: List<LootPredicate> = emptyList(),
) : CompositeEntryBase<S>(children, conditions) {
    companion object {
        val SERIALIZER: SimpleSerializer<SequentialEntry<*>> = makeSerializer(::SequentialEntry)
    }

    override fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S> {
        return when (children.size) {
            0 -> ComposableEntryContainer.alwaysTrue()
            1 -> children[0]
            2 -> children[0].and(children[1])
            else -> ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
                for (composableEntryContainer in children) {
                    if (context.selectEverything) {
                        // If we are iterating, we expand all composable entries
                        composableEntryContainer.expand(context, dataConsumer)
                        continue
                    }
                    if (!composableEntryContainer.expand(context, dataConsumer)) {
                        return@ComposableEntryContainer false
                    }
                }
                true
            }
        }
    }
}