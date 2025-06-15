package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.context.LootContext
import cc.mewcraft.wakame.random4.predicate.LootPredicate
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 若满足指定条件, 从 [children] 中选择第一个满足条件的子项.
 */
@ConfigSerializable
class AlternativesEntry<S>(
    children: List<LootPoolEntryContainer<S>>,
    conditions: List<LootPredicate>,
) : CompositeEntryBase<S>(children, conditions) {

    override fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S> {
        return when (children.size) {
            0 -> ComposableEntryContainer.alwaysFalse()
            1 -> children[0]
            2 -> children[0].or(children[1])
            else -> ComposableEntryContainer { context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit ->
                for (composableEntryContainer in children) {
                    if (composableEntryContainer.expand(context, dataConsumer)) {
                        return@ComposableEntryContainer true
                    }
                }
                false
            }
        }
    }
}