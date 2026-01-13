package cc.mewcraft.wakame.loot.predicate

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import io.leangen.geantyref.TypeToken

/**
 * 用于标识 [LootPredicate] 的类型, 以及提供序列化器.
 *
 * @param T [LootPredicate] 的类型
 */
sealed interface LootPredicateType<out T : LootPredicate> {
    companion object {
        fun <T : LootPredicate> create(
            token: TypeToken<T>,
            serializer: SimpleSerializer<T>,
        ): LootPredicateType<T> = Simple(token, serializer)
    }

    /**
     * 类型 [T] 的 TypeToken, 用于给 configurate 传递类型信息 [T]
     */
    val typeToken: TypeToken<out T>

    /**
     * [T] 的序列化器.
     */
    val serializer: SimpleSerializer<out T>

    private class Simple<T : LootPredicate>(
        override val typeToken: TypeToken<out T>,
        override val serializer: SimpleSerializer<out T>,
    ) : LootPredicateType<T> {
        override fun toString(): String = "SimpleLootPredicateType(typeToken=$typeToken, serializer=$serializer)"
    }
}
