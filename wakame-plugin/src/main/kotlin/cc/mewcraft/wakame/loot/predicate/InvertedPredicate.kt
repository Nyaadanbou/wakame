package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.loot.context.LootContext

class InvertedPredicate(
    val term: LootPredicate,
) : LootPredicate {
    companion object {
        @JvmField
        val SERIALIZER: SimpleSerializer<InvertedPredicate> = SimpleSerializer { type, node ->
            val term = node.node("term").require<LootPredicate>()
            InvertedPredicate(term)
        }
    }

    override fun invoke(context: LootContext): Boolean {
        return !term.invoke(context)
    }
}
