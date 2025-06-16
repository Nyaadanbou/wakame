package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

sealed interface LootPoolEntryType<T : ComposableEntryContainer<*>> {
    companion object {
        fun <T : ComposableEntryContainer<*>> create(
            token: TypeToken<T>,
            serializer: TypeSerializer2<T>
        ): LootPoolEntryType<T> = Simple(token, serializer)
    }

    // 类型 T 的 TypeToken, 用于给 configurate 传递类型信息 T
    val typeToken: TypeToken<T>

    /**
     * 返回空表示数据类型 [T] 可以直接使用现有的 [TypeSerializerCollection] 来完成序列化操作.
     * 但如果数据类型 [T] 需要依赖额外的 [TypeSerializer] 来完成序列化操作, 可以在这里返回.
     */
    val serializer: TypeSerializer2<T>

    private class Simple<T : ComposableEntryContainer<*>>(
        override val typeToken: TypeToken<T>,
        override val serializer: TypeSerializer2<T>,
    ) : LootPoolEntryType<T> {
        override fun toString(): String = "SimpleLootPoolEntryType(typeToken=$typeToken, serializer=$serializer)"
    }
}