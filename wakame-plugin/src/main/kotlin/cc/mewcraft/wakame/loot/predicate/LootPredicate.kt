package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.registry.BuiltInRegistries
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException

typealias LootContextPredicate = (LootContext) -> Boolean

fun interface LootPredicate : LootContextPredicate {
    companion object {
        internal fun serializer(): SimpleSerializer<LootPredicate> {
            return SimpleSerializer { type, node ->
                val typeName = node.node("type").get<String>()
                val lootPredicateType = if (typeName == null) {
                    LootPredicates.ALL_OF // 默认是 AllOfPredicate
                } else {
                    BuiltInRegistries.LOOT_PREDICATE_TYPE[typeName] ?: throw SerializationException(node, type, "Unknown loot predicate type: $typeName")
                }
                lootPredicateType.serializer.deserialize(type, node)
                    ?: throw SerializationException(node, type, "Failed to deserialize loot predicate of type: $typeName")
            }
        }
    }

    override fun invoke(context: LootContext): Boolean
}
