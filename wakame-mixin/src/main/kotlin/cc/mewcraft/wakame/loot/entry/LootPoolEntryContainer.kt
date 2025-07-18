package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate

abstract class LootPoolEntryContainer<S>(
    val conditions: List<LootPredicate>,
) : ComposableEntryContainer<S> {
    protected fun canRun(context: LootContext): Boolean {
        return context.selectEverything || this.conditions.all { it.invoke(context) }
    }
}
