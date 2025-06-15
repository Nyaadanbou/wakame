package cc.mewcraft.wakame.random4.predicate

import cc.mewcraft.wakame.random4.context.LootContext

fun interface LootPredicate {
    fun test(context: LootContext): Boolean
}