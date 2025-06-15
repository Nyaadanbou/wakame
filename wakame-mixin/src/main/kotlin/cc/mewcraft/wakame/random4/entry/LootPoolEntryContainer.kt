package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.random4.context.LootContext
import cc.mewcraft.wakame.random4.predicate.LootPredicate

abstract class LootPoolEntryContainer<S>(
    val conditions: List<LootPredicate>,
) : ComposableEntryContainer<S> {
    protected fun canRun(context: LootContext): Boolean {
        return this.conditions.all { it.test(context) }
    }
}