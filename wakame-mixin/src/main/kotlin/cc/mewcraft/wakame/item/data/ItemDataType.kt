package cc.mewcraft.wakame.item.data

import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

sealed interface ItemDataType<T> {

    companion object {

        fun <T> builder(typeToken: TypeToken<T>): Builder<T> {
            return Builder(typeToken)
        }

    }

    // 类型 T 的 TypeToken, 用于给 configurate 传递类型信息 T
    val typeToken: TypeToken<T>

    /**
     * 如果 [persistent] 是 `true`, 则表示数据类型 [T] 将会被持久化.
     */
    val persistent: Boolean

    /**
     * 返回空表示数据类型 [T] 可以直接使用现有的 [TypeSerializerCollection] 来完成序列化操作.
     * 但如果数据类型 [T] 需要依赖额外的 [TypeSerializer] 来完成序列化操作, 可以在这里返回.
     */
    val serializers: TypeSerializerCollection?

    class Builder<T>(
        private val typeToken: TypeToken<T>,
    ) {
        private var persistent: Boolean = false
        private var serializers: TypeSerializerCollection? = null

        fun persistent(persistent: Boolean): Builder<T> {
            this.persistent = persistent
            return this
        }

        fun serializers(serializers: TypeSerializerCollection): Builder<T> {
            this.serializers = serializers
            return this
        }

        fun serializers(block: TypeSerializerCollection.Builder.() -> Unit): Builder<T> {
            this.serializers = TypeSerializerCollection.builder().apply(block).build()
            return this
        }

        fun build(): ItemDataType<T> {
            return Simple(typeToken, persistent, serializers)
        }

        private class Simple<T>(
            override val typeToken: TypeToken<T>,
            override val persistent: Boolean,
            override val serializers: TypeSerializerCollection?,
        ) : ItemDataType<T> {
            override fun toString(): String = "ItemDataType(type=${typeToken.type},persistent=$persistent,serializers=$serializers)"
        }
    }

}