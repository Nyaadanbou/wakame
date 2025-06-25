package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import io.leangen.geantyref.TypeToken

/**
 * 用于标识 [ComposableEntryContainer] 的类型, 以及提供序列化器.
 *
 * @param T [ComposableEntryContainer] 的类型
 */
sealed interface LootPoolEntryType<out T : ComposableEntryContainer<*>> {
    companion object {
        fun <T : ComposableEntryContainer<*>> create(
            token: TypeToken<T>,
            serializer: TypeSerializer2<T>,
        ): LootPoolEntryType<T> = Simple(token, serializer)
    }

    /**
     * 类型 [T] 的 TypeToken, 用于给 configurate 传递类型信息 [T]
     */
    val typeToken: TypeToken<out T>

    /**
     * [T] 的序列化器.
     */
    val serializer: TypeSerializer2<out T>

    private class Simple<T : ComposableEntryContainer<*>>(
        override val typeToken: TypeToken<out T>,
        override val serializer: TypeSerializer2<out T>,
    ) : LootPoolEntryType<T> {
        override fun toString(): String = "SimpleLootPoolEntryType(typeToken=$typeToken, serializer=$serializer)"
    }
}
