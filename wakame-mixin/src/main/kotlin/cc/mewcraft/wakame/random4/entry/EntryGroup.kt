package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.predicate.LootPredicate
import cc.mewcraft.wakame.random4.context.LootContext
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class EntryGroup<S>(
    children: List<LootPoolEntryContainer<S>>,
    conditions: List<LootPredicate>,
) : CompositeEntryBase<S>(children, conditions) {

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