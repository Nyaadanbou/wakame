package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.predicate.LootPredicate
import cc.mewcraft.wakame.random4.context.LootContext

abstract class CompositeEntryBase<S>(
    protected val children: List<LootPoolEntryContainer<S>>,
    conditions: List<LootPredicate>,
) : LootPoolEntryContainer<S>(conditions) {
    private val composedChildren: ComposableEntryContainer<S> = compose(children)

    protected abstract fun compose(children: List<ComposableEntryContainer<S>>): ComposableEntryContainer<S>

    final override fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean {
        return this.canRun(context) && this.composedChildren.expand(context, dataConsumer)
    }
}