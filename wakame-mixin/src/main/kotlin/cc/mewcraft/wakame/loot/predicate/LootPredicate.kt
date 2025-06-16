package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

fun interface LootPredicate {
    companion object {
        val SERIALIZER: TypeSerializer2<LootPredicate> = Serializer

        object AlwaysTruePredicate : LootPredicate {
            override fun test(context: LootContext): Boolean = true

            override fun toString(): String = "AlwaysTruePredicate"
        }

        object AlwaysFalsePredicate : LootPredicate {
            override fun test(context: LootContext): Boolean = false

            override fun toString(): String = "AlwaysFalsePredicate"
        }
    }

    fun test(context: LootContext): Boolean

    private object  Serializer : TypeSerializer2<LootPredicate> {
        override fun deserialize(type: Type, node: ConfigurationNode): LootPredicate? {
            val typeName = node.node("type").require<String>()
            return when (typeName) {
                "always_true" -> AlwaysTruePredicate
                "always_false" -> AlwaysFalsePredicate
                else -> throw IllegalArgumentException("Unknown LootPredicate type: $typeName")
            }
        }
    }
}