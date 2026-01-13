package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate

class EntryGroup<S>(
    children: List<LootPoolEntryContainer<S>>,
    conditions: List<LootPredicate>,
) : CompositeEntryBase<S>(children, conditions) {
    companion object {
        val SERIALIZER: SimpleSerializer<EntryGroup<*>> = makeSerializer(::EntryGroup)
    }

    override fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S> {
        return when (children.size) {
            0 -> ComposableEntryContainer.alwaysTrue()
            1 -> children[0]
            2 -> {
                val composableEntryContainer = children[0]
                val composableEntryContainer1 = children[1]
                ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
                    composableEntryContainer.expand(context, dataConsumer)
                    composableEntryContainer1.expand(context, dataConsumer)
                    true
                }
            }

            else -> ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
                for (composableEntryContainer2 in children) {
                    composableEntryContainer2.expand(context, dataConsumer)
                }
                true
            }
        }
    }
}