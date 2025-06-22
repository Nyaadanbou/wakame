package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

typealias LootContextPredicate = (LootContext) -> Boolean

fun interface LootPredicate : LootContextPredicate {
    companion object {
        val SERIALIZER: TypeSerializer2<LootPredicate> = Serializer
    }

    override fun invoke(context: LootContext): Boolean

    private object Serializer : TypeSerializer2<LootPredicate> {
        override fun deserialize(type: Type, node: ConfigurationNode): LootPredicate {
            val typeName = node.node("type").get<String>()
            val lootPredicateType = if (typeName == null) {
                LootPredicates.ALL_OF // 默认是 AllOfPredicate
            } else {
                BuiltInRegistries.LOOT_PREDICATE_TYPE[typeName] ?: throw SerializationException(node, type, "Unknown loot predicate type: $typeName")
            }
            return lootPredicateType.serializer.deserialize(type, node)
                ?: throw SerializationException(node, type, "Failed to deserialize loot predicate of type: $typeName")
        }
    }
}
