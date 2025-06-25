package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require

class InvertedPredicate(
    val term: LootPredicate,
) : LootPredicate {
    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<InvertedPredicate> = TypeSerializer2 { type, node ->
            val term = node.node("term").require<LootPredicate>()
            InvertedPredicate(term)
        }
    }

    override fun invoke(context: LootContext): Boolean {
        return !term.invoke(context)
    }
}
